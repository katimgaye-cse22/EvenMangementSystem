package com.campus.eventmanagement.exception;

/**
 * Thrown when a student tries to register for an event that has reached capacity —
 * mapped to HTTP 409 (Conflict) by GlobalExceptionHandler.
 */
public class EventFullException extends RuntimeException {

    public EventFullException(String message) {
        super(message);
    }
}
