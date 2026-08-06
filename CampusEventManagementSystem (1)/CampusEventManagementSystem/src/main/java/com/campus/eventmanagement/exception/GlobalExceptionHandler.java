package com.campus.eventmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception handling for every @RestController (course concept:
 * Exception Handling). Keeps controllers free of try/catch noise and gives
 * the frontend a consistent JSON error shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(EventFullException.class)
    public ResponseEntity<Map<String, Object>> handleEventFull(EventFullException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Thrown by Hibernate when two requests race to update the same
     * @Version-ed row (e.g. two near-simultaneous registrations on an
     * event with one seat left). The loser gets a clear "try again"
     * message instead of a raw stack trace.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return buildResponse(HttpStatus.CONFLICT,
                "This event was just updated by someone else. Please refresh and try again.");
    }

    /**
     * Covers "business rule" conflicts we raise with plain exceptions
     * (duplicate email/student id at signup, already-registered-for-event, etc.)
     * so services don't need bespoke exception classes for every conflict case.
     */
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    /**
     * Bean Validation failures (@Valid on @RequestBody DTOs) — returns a
     * field -> message map so the frontend can show inline errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionSystem(org.springframework.transaction.TransactionSystemException ex) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof jakarta.validation.ConstraintViolationException cve) {
            Map<String, String> fieldErrors = new LinkedHashMap<>();
            for (jakarta.validation.ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            String detailMsg = fieldErrors.isEmpty() ? "" : ": " + String.join("; ", fieldErrors.values());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", HttpStatus.BAD_REQUEST.value());
            body.put("message", "Validation failed while saving" + detailMsg);
            body.put("errors", fieldErrors);
            return ResponseEntity.badRequest().body(body);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not save changes: " + cause.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
