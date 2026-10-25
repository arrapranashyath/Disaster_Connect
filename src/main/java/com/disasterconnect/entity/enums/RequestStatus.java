package com.disasterconnect.entity.enums;

/**
 * Status lifecycle for emergency assistance requests per SRS REQ-HELP-4 and DDS Section 3.4.
 * Lifecycle: PENDING -> ACCEPTED / ASSIGNED -> IN_PROGRESS -> COMPLETED / RESOLVED (or CANCELLED).
 */
public enum RequestStatus {
    PENDING,
    ACCEPTED,
    ASSIGNED,
    IN_PROGRESS,
    COMPLETED,
    RESOLVED,
    CANCELLED;

    public boolean isOpen() {
        return this == PENDING;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == RESOLVED || this == CANCELLED;
    }
}
