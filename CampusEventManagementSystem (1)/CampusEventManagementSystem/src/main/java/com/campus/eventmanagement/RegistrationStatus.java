package com.campus.eventmanagement.enums;

/**
 * Status of a student's registration for an event.
 * We keep cancelled registrations (soft delete) instead of removing rows,
 * so capacity history and reporting stay accurate.
 */
public enum RegistrationStatus {
    REGISTERED,
    CANCELLED
}
