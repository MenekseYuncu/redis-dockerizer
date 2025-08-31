package com.redisdockerizer.keymanagement.keymanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

/**
 * Custom exception to represent unauthorized access or authentication failures.
 * <p>
 * This exception is annotated with {@code @ResponseStatus} to automatically set the HTTP
 * response status to {@code HttpStatus.UNAUTHORIZED (401)} when it is thrown in a Spring application.
 * It is typically used in scenarios where user authentication fails, such as invalid credentials
 * or an inactive user attempting to perform an action requiring authorization.
 * <p>
 * The exception extends {@code RuntimeException}, making it an unchecked exception.
 * It can be thrown at runtime without being explicitly declared in method signatures.
 * <p>
 * Constructor takes a message parameter that provides details about the specific unauthorized condition.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 946800093606171418L;

    public UnauthorizedException(String message) {
        super(message);
    }
}
