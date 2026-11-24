package com.Ticketr.CustomerService.domain;

public enum TicketStatus {
    /** Just created, not yet routed. Transient — the assignment engine moves it to ASSIGNED immediately. */
    OPEN,
    /** Routed to a department and an agent, waiting for the agent to start work. */
    ASSIGNED,
    /** The assigned agent is actively working the ticket. */
    IN_PROGRESS,
    /** The assigned agent believes the issue is fixed. NOT closed yet — awaits the closing checkpoint. */
    RESOLVED,
    /** Terminal state. Only reachable from RESOLVED, and only by the assigned agent. */
    CLOSED,
    /** The customer was not satisfied with the resolution and reopened the ticket. */
    REOPENED
}
