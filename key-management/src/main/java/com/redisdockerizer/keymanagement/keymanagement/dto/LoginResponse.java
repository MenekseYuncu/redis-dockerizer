package com.redisdockerizer.keymanagement.keymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the response for a login operation.
 * This class encapsulates the result of a user login attempt,
 * including session details, user information, and a status message.
 * <p>
 * The class provides the following information:
 * - sessionId: Unique identifier for the user session.
 * - userId: Identifier of the user associated with the session.
 * - username: Name of the user who initiated the login.
 * - roles: List of roles assigned to the user.
 * - expiresAt: Expiration timestamp of the session.
 * - message: Status or result message of the login operation (e.g., success or failure).
 * <p>
 * Instances of this class are typically used to send structured
 * responses from the login API endpoint to the client.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String sessionId;
    private Long userId;
    private String username;
    private List<String> roles;
    private LocalDateTime expiresAt;
    private String message;
}
