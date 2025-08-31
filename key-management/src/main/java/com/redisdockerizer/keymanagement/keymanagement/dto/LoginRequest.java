package com.redisdockerizer.keymanagement.keymanagement.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Represents a login request containing the user's credentials.
 * This class is used to encapsulate the authentication details
 * provided by the user during login.
 * <p>
 * The fields are validated to ensure essential data is provided:
 * - `username` must not be blank.
 * - `password` must not be blank.
 * <p>
 * This class is typically used in API endpoints to process incoming
 * login requests and validate user credentials.
 */
public record LoginRequest(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {

}