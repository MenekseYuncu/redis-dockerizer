package com.redisdockerizer.sessionmanagement.session.util;

import com.redisdockerizer.sessionmanagement.session.dto.SessionResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * GlobalExceptionHandler is a centralized exception handling class annotated with
 * @RestControllerAdvice to handle exceptions across the whole application consistently.
 * It catches specific exceptions and sends appropriate HTTP status codes and messages
 * in response to the client. This promotes better user experience and robust error management.
 * The log annotations (@Slf4j) are leveraged for logging details about the exceptions.
 * <p>
 * Exception Handlers:
 * - NoSuchElementException: Triggers a 404 NOT_FOUND response when the requested element is not found.
 * - ConstraintViolationException: Triggers a 400 BAD_REQUEST response when validation constraints are violated.
 * - IllegalArgumentException: Triggers a 400 BAD_REQUEST response for illegal arguments in the request.
 * - Exception: Catches generic exceptions and triggers a 500 INTERNAL_SERVER_ERROR response for unexpected errors.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public SessionResponse handleUserNotFound(NoSuchElementException e) {
        log.warn("User not found: {}", e.getMessage());
        return SessionResponse.builder()
                .message("User not found: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SessionResponse handleValidationError(ConstraintViolationException e) {
        log.warn("Validation error: {}", e.getMessage());
        return SessionResponse.builder()
                .message("Validation failed: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public SessionResponse handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid argument: {}", e.getMessage());
        return SessionResponse.builder()
                .message("Invalid request: " + e.getMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SessionResponse handleGenericError(Exception e) {
        log.error("Unexpected error occurred", e);
        return SessionResponse.builder()
                .message("An unexpected error occurred")
                .build();
    }
}