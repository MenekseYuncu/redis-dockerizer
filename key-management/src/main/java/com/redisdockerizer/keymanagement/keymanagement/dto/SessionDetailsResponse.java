package com.redisdockerizer.keymanagement.keymanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents the response containing details of a specific user session.
 * This record encapsulates all necessary information about a session, including its
 * identifier, associated user details, roles, timestamps, and client-related information.
 * <p>
 * Fields:
 * - sessionId: The unique identifier of the session.
 * - userId: The identifier of the user associated with the session.
 * - username: The username of the individual owning the session.
 * - roles: A list of roles assigned to the user within the session.
 * - createdAt: The timestamp marking the creation of the session.
 * - expiresAt: The timestamp indicating when the session will expire.
 * - clientIp: The IP address of the client from which the session originated.
 * <p>
 * Instances of this class are typically used in API responses to provide comprehensive
 * details regarding an active session, helping clients monitor or manage their sessions.
 */
public record SessionDetailsResponse(

        String sessionId,
        Long userId,
        String username,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        String clientIp

) {
}
