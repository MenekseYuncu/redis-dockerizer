package com.redisdockerizer.keymanagement.keymanagement.dto;

import java.util.List;

/**
 * Represents the response of a session validation operation.
 * This record encapsulates the outcome of a session validation request,
 * including the validity status, user details, roles, and an optional error message.
 * <p>
 * Fields:
 * - valid: A boolean indicating whether the session is valid.
 * - userId: The identifier of the user associated with the session, if valid.
 * - username: The username of the user associated with the session, if valid.
 * - roles: A list of roles assigned to the user for the session, if valid.
 * - Error: A descriptive message indicating the reason for an invalid session, if applicable.
 * <p>
 * This response is typically used in API operations that check the status
 * of a user's session and return the appropriate details or error messages.
 */
public record ValidateSessionResponse(
        boolean valid,
        Long userId,
        String username,
        List<String> roles,
        String error
) {
}
