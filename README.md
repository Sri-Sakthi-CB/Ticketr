
# Ticketr – Customer Support Ticketing System

A full-stack customer support ticketing application built with **Java, Spring Boot, React, and PostgreSQL**. Ticketr streamlines customer issue management through automated ticket assignment, department-based routing, role-based access control, and a structured ticket lifecycle.

## Key Features

- **Role-Based Access Control:** Separate permissions and dashboards for Admins, Agents, and Customers.
- **Automated Ticket Assignment:** Routes tickets to the appropriate department and assigns them to the active agent with the fewest open tickets.
- **Ticket Lifecycle Management:** Structured workflow from ticket creation to closure, including customer-driven reopening.
- **Priority-Based SLA:** Different resolution targets based on ticket priority.
- **Audit History:** Maintains a history of ticket status changes.
- **JWT Authentication:** Secures API endpoints using JSON Web Tokens.
- **Mandatory Resolution Checkpoint:** Only the agent assigned to a ticket can close it, and only after resolving it.

## Technology Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4 |
| Security | Spring Security, JWT |
| Database | PostgreSQL |
| ORM | Spring Data JPA, Hibernate |
| Frontend | React 19, TypeScript |
| UI | Material UI (MUI) |
| Routing | React Router |
| HTTP Client | Axios |
| Build Tools | Maven, Vite |
| Code Quality | Oxlint |

## Application Architecture

The application follows a client-server architecture.

- **Frontend:** React application responsible for the user interface and API communication.
- **Backend:** Spring Boot REST APIs responsible for business logic, authentication, authorization, ticket assignment, and lifecycle validation.
- **Database:** PostgreSQL stores users, departments, tickets, and ticket event history.

## Ticket Lifecycle

```text
             Customer Creates Ticket
                       |
                       v
                      OPEN
                       |
             Automated Assignment
                       |
                       v
                    ASSIGNED
                       |
               Agent Starts Work
                       |
                       v
                  IN_PROGRESS
                       |
                 Agent Resolves
                       |
                       v
                   RESOLVED
                       |
             +---------+---------+
             |                   |
       Customer Accepts    Customer Reopens
             |                   |
             v                   v
           CLOSED             REOPENED
                                 |
                         Assigned Agent
                           Continues Work
                                 |
                                 v
                              RESOLVED
```

### Mandatory Closure Rule

Ticket closure is enforced by the backend:

- Only the agent assigned to the ticket can close it.
- A ticket must be in `RESOLVED` status before it can be closed.
- Customers, other agents, and administrators cannot close tickets.
- The frontend displays the Close Ticket action only when the backend confirms that the current user is authorized.

This ensures that ticket closure follows a controlled and validated workflow.

## Automated Ticket Routing

When a customer creates a ticket:

1. The issue type determines the appropriate department.
2. The system identifies active agents in that department.
3. It checks the number of open tickets assigned to each agent.
4. The ticket is assigned to the agent with the fewest open tickets.

### Supported Departments

- Billing
- Technical Support
- Account & Access
- General Inquiries

## Priority-Based SLA

| Priority | Resolution Target |
|---|---|
| Critical | 4 hours |
| High | 24 hours |
| Medium | 72 hours |
| Low | 120 hours |

## User Roles

| Role | Responsibilities |
|---|---|
| Admin | View and manage tickets across the application |
| Agent | View assigned tickets, start work, resolve, and close tickets |
| Customer | Create tickets, track progress, and reopen resolved tickets |

## REST API Overview

**Base URL:** `http://localhost:8080`

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new customer |
| POST | `/api/auth/login` | Authenticate and obtain JWT |

### Departments

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/departments` | Retrieve available departments |

### Tickets

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/tickets` | Create a ticket with automated assignment |
| GET | `/api/tickets` | Retrieve tickets based on user role |
| GET | `/api/tickets/{id}` | Retrieve ticket details |
| GET | `/api/tickets/{id}/events` | Retrieve ticket audit history |
| PATCH | `/api/tickets/{id}/start` | Start an assigned ticket |
| PATCH | `/api/tickets/{id}/resolve` | Resolve a ticket |
| PATCH | `/api/tickets/{id}/close` | Close a resolved ticket |
| PATCH | `/api/tickets/{id}/reopen` | Reopen a resolved ticket |

All ticket operations enforce authentication, authorization, and valid lifecycle transitions at the backend.

## Project Setup

### Prerequisites

Install the following before running the application:

- Java 21
- PostgreSQL
- Node.js and npm
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/Sri-Sakthi-CB/Ticketr.git
cd Ticketr
```

### 2. Configure PostgreSQL

Create the database and user:

```sql
CREATE USER ticketing WITH PASSWORD 'ticketing';
CREATE DATABASE ticketing OWNER ticketing;
```

The default local database configuration is available in the backend's `application.properties`.

For deployment, configure database credentials through environment variables instead of using default credentials.

### 3. Start the Backend

Navigate to the backend directory:

```bash
cd CustomerService
```

**Windows:**

```bash
mvnw.cmd spring-boot:run
```

**Linux/macOS:**

```bash
./mvnw spring-boot:run
```

Backend URL: `http://localhost:8080`

### 4. Start the Frontend

Open another terminal from the repository root:

```bash
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## Demo Accounts

The application includes seeded demo accounts for testing different roles.

| Role | Email |
|---|---|
| Admin | admin@demo.com |
| Billing Agent | agent.billing@demo.com |
| Technical Agent 1 | agent.technical1@demo.com |
| Technical Agent 2 | agent.technical2@demo.com |
| Account Agent | agent.account@demo.com |
| General Inquiries Agent | agent.general@demo.com |
| Customer | customer@demo.com |

**Password for all demo accounts:** `password123`

New customers can also register through the application.

## Security Considerations

- JWT-based authentication for protected APIs.
- Role-based authorization for application operations.
- Backend validation of ticket ownership and assigned-agent permissions.
- Server-side enforcement of ticket lifecycle transitions.
- Sensitive configuration such as JWT secrets and database credentials should be supplied through environment variables in production.

## Future Enhancements

- Email notifications for ticket updates.
- SLA breach notifications and escalation workflows.
- Advanced ticket filtering and search.
- Dashboard analytics and reporting.
- Unit and integration test coverage.
- Production database migration management using Flyway or Liquibase.

## Author

**Sri Sakthi**

GitHub: [Sri-Sakthi-CB](https://github.com/Sri-Sakthi-CB)
