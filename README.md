# Ticketr — Customer Support Ticketing App

A support ticketing system: customers raise tickets with a priority, tickets are auto-routed to
the right department and agent, and closing a ticket is gated behind a checkpoint — only the
agent the ticket is assigned to can close it, and only after they've marked it resolved.

## Stack

- **Backend**: Spring Boot 4 (Java 21), Spring Security + JWT, Spring Data JPA, PostgreSQL — `CustomerService/`
- **Frontend**: React 19 + TypeScript, Vite, MUI, React Router — `frontend/`

## The ticket lifecycle (the checkpoint)

```
OPEN --(auto-routed)--> ASSIGNED --(agent starts)--> IN_PROGRESS --(agent resolves)--> RESOLVED --(agent closes)--> CLOSED
                                                            ^                              |
                                                            |                              v
                                                            +---------- REOPENED <---- (customer reopens)
```

- **Routing**: on creation, the issue type picks the department (Billing / Technical Support /
  Account & Access / General Inquiries), and the ticket is assigned to whichever active agent in
  that department currently has the fewest open tickets. Priority sets the SLA due date
  (Critical: 4h, High: 24h, Medium: 72h, Low: 120h).
- **The checkpoint**: `RESOLVED -> CLOSED` is the one transition that isn't a plain status edit.
  It is enforced server-side in `TicketService.close()` — the caller must be the ticket's
  `assignedAgent`, and the ticket must currently be `RESOLVED`. No one else (not the customer, not
  another agent, not an admin) can close a ticket, and the API rejects the attempt with 403/409 if
  they try. The frontend also only renders the "Close Ticket" button when the backend says
  `canClose: true` for the logged-in user, so this isn't just a client-side convention.
- Customers can reopen a `RESOLVED` ticket they own if they're not satisfied, which sends it back
  to the same assigned agent.

## Running it

### 1. Database

Pick one:

**Docker** (not installed on this machine as of writing, but the compose file is ready):
```
docker compose up -d
```
This starts Postgres on `localhost:5432` with db/user/password all `ticketing`.

**Local PostgreSQL** (you already have PostgreSQL 17 running as a Windows service): connect with
your own superuser credentials and run:
```sql
CREATE USER ticketing WITH PASSWORD 'ticketing';
CREATE DATABASE ticketing OWNER ticketing;
```

Either way, `CustomerService/src/main/resources/application.properties` already points at
`jdbc:postgresql://localhost:5432/ticketing` / `ticketing` / `ticketing`. Change it there (or via
env vars) if you'd rather use different credentials.

### 2. Backend

```
cd CustomerService
./mvnw spring-boot:run
```
Runs on `http://localhost:8080`. On first boot it seeds departments, routing rules, and demo
accounts (all password `password123`):

| Role   | Email                          | Department          |
|--------|--------------------------------|----------------------|
| Admin  | admin@demo.com                 | —                    |
| Agent  | agent.billing@demo.com         | Billing              |
| Agent  | agent.technical1@demo.com      | Technical Support    |
| Agent  | agent.technical2@demo.com      | Technical Support    |
| Agent  | agent.account@demo.com         | Account & Access     |
| Agent  | agent.general@demo.com         | General Inquiries    |
| Customer | customer@demo.com             | —                    |

New customers can also self-register from the login page.

### 3. Frontend

```
cd frontend
npm install
npm run dev
```
Runs on `http://localhost:5173` and proxies `/api` to the backend.

## API summary

| Method | Path                     | Who                    | What |
|--------|--------------------------|------------------------|------|
| POST   | /api/auth/register       | anyone                 | Create a customer account |
| POST   | /api/auth/login          | anyone                 | Get a JWT |
| GET    | /api/departments         | any authenticated user | List departments |
| POST   | /api/tickets             | customer               | Raise a ticket (auto-routed & assigned) |
| GET    | /api/tickets             | any                     | List tickets (own / assigned / all, by role) |
| GET    | /api/tickets/{id}        | owner / assigned agent / admin | Ticket detail |
| GET    | /api/tickets/{id}/events | same as above           | Status-change audit trail |
| PATCH  | /api/tickets/{id}/start  | assigned agent          | ASSIGNED -> IN_PROGRESS |
| PATCH  | /api/tickets/{id}/resolve| assigned agent          | IN_PROGRESS/REOPENED -> RESOLVED |
| PATCH  | /api/tickets/{id}/close  | **assigned agent only** | RESOLVED -> CLOSED (the checkpoint) |
| PATCH  | /api/tickets/{id}/reopen | owning customer         | RESOLVED -> REOPENED |

## Deploying to Render (get a public URL)

The repo is already prepped for this: `CustomerService/Dockerfile` builds the backend, and
`render.yaml` describes all three pieces (backend, static frontend, Postgres) as a Render
Blueprint. Local git is initialized and the first commit is made; push it to GitHub yourself,
then:

### 1. Push to GitHub

Create an empty repo on github.com (public or private — either works with Render), then:
```
git remote add origin <your-repo-url>
git branch -M main
git push -u origin main
```

### 2. Deploy via Blueprint (try this first)

1. Sign up / log in at render.com and connect your GitHub account.
2. **New +** → **Blueprint** → pick this repo. Render reads `render.yaml` and proposes a
   Postgres database, a `ticketr-backend` web service (Docker), and a `ticketr-frontend`
   static site.
3. Click **Apply**. `JWT_SECRET` is auto-generated; the datasource env vars are wired to the
   database automatically.
4. Render's Blueprint schema changes over time — if it rejects a field, use the manual steps
   below instead (they're more resilient to UI changes).

### 3. Manual fallback, if the Blueprint doesn't apply cleanly

1. **New +** → **PostgreSQL** → name it `ticketr-db`, free plan. Once it's up, note the
   **Host**, **Port**, **Database**, **Username**, **Password** shown on its page.
2. **New +** → **Web Service** → connect the repo → set **Root Directory** to `CustomerService`
   and **Runtime** to Docker (it'll pick up the `Dockerfile`). Free plan. Add environment
   variables:
   - `PGHOST`, `PGPORT`, `PGDATABASE`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
     — from the database page in step 1
   - `JWT_SECRET` — any long random string
   - `CORS_ALLOWED_ORIGINS` — the frontend URL from step 3 (you'll fill this in after creating it)

   Deploy, then copy the resulting URL (e.g. `https://ticketr-backend-xxxx.onrender.com`).
3. **New +** → **Static Site** → connect the repo → **Root Directory** `frontend`, **Build
   Command** `npm install && npm run build`, **Publish Directory** `dist`. Add environment
   variable `VITE_API_BASE_URL` = `<backend URL from step 2>/api`. Under **Redirects/Rewrites**,
   add a rule: source `/*` → destination `/index.html` → type Rewrite (needed for client-side
   routing). Deploy, then copy this URL too.
4. Go back to the backend service's environment variables and set `CORS_ALLOWED_ORIGINS` to the
   frontend's actual URL, then trigger a redeploy of the backend.

### Things worth knowing about the free tier

- The free web service **sleeps after 15 minutes idle** and takes several seconds to wake on the
  next request — expect a slow first load when you share the link.
- Render's free Postgres plan **expires after a set period** (historically ~30–90 days,
  double-check current terms in the Render dashboard) — fine for a demo, not for anything
  long-lived. Upgrade the database plan before that matters.
- `VITE_API_BASE_URL` is baked into the frontend at build time. If the backend's actual URL ends
  up different from what you expected (Render appends a random suffix if the name is taken), update
  the env var and manually redeploy the static site.
- Rotate `JWT_SECRET` to something you generate yourself, not the demo value committed in
  `application.properties` (that value only takes effect if you *don't* set the env var).

## Notes / things you may want to change later

- Departments and routing rules are a fixed starter set seeded by `DataSeeder`, per your choice of
  "fixed starter set" over an admin-managed one. To add a department or change routing, edit the
  seeder (or add an admin UI later).
- `spring.jpa.hibernate.ddl-auto=update` is used for simplicity; swap in Flyway/Liquibase
  migrations before this goes anywhere near production data.
- The JWT secret in `application.properties` is a demo default — override `JWT_SECRET` for any
  real deployment.
