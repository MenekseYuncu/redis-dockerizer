package com.redisdockerizer.keymanagement.keymanagement.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The UserSession class represents session information for an authenticated user.
 * It contains details such as session ID, user ID, username, roles, and session timing.
 * This class is typically used to store or track session data for a user interacting
 * with an application.
 * <p>
 * Thread safety is not guaranteed for instances of this class. Ensure proper
 * synchronization in multithreaded environments if necessary.
 * <p>
 * Attributes:
 * - sessionId: Unique identifier for the session.
 * - userId: Identifier of the user associated with the session.
 * - username: The username of the user associated with the session.
 * - roles: List of roles assigned to the user for the current session.
 * - createdAt: Timestamp indicating when the session was created.
 * - expiresAt: Timestamp indicating when the session will expire.
 * - clientIp: IP address of the client initiating the session.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    private String sessionId;
    private Long userId;
    private String username;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String clientIp;

}
