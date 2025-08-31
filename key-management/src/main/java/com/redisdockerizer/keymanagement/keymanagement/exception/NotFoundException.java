package com.redisdockerizer.keymanagement.keymanagement.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Custom exception to represent situations where a requested resource cannot be found.
 * <p>
 * This exception is annotated with {@code @ResponseStatus} to automatically set the HTTP response
 * status to {@code HttpStatus.NOT_FOUND (404)} when it is thrown in a Spring application.
 * It is typically used in scenarios where a specific entity or resource cannot be located,
 * such as a missing database record or an invalid request path.
 * <p>
 * The exception extends {@code RuntimeException}, making it an unchecked exception.
 * It can be thrown at runtime without being explicitly declared in method signatures.
 * <p>
 * Constructor takes a message parameter that allows providing additional details
 * about the failed resource lookup.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8995070657958573368L;

    public NotFoundException(String message) {
        super(message);
    }
}
