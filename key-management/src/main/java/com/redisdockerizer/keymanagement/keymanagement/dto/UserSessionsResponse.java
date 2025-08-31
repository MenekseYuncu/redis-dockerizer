package com.redisdockerizer.keymanagement.keymanagement.dto;

import java.util.List;

/**
 * Represents the response containing active session details for a specific user.
 * This record provides information about the user's active sessions, the count of those sessions,
 * and the user identifier to whom the sessions belong.
 * <p>
 * Fields:
 * - userId: The identifier of the user whose session details are being queried.
 * - activeSessions: A list of session identifiers corresponding to the user's active sessions.
 * - Count: The total number of active sessions associated with the user.
 * <p>
 * This response is typically used in API endpoints to retrieve and return active session
 * information for a user, allowing clients to monitor or manage user sessions.
 */
public record UserSessionsResponse(
        Long userId,
        List<String> activeSessions,
        int count
) {
}
