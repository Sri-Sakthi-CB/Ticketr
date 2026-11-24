package com.Ticketr.CustomerService.domain;

public enum Priority {
    LOW(120),
    MEDIUM(72),
    HIGH(24),
    CRITICAL(4);

    /** Hours allowed before the ticket's SLA due date, based on priority. */
    private final int slaHours;

    Priority(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}
