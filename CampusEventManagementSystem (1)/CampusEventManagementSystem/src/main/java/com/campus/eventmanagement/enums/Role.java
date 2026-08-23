package com.campus.eventmanagement.enums;

/**
 * Roles a User can have in the system.
 * Used by Spring Security for authorization (ROLE_STUDENT / ROLE_ADMIN).
 */
public enum Role {
    STUDENT,
    ADMIN
}
