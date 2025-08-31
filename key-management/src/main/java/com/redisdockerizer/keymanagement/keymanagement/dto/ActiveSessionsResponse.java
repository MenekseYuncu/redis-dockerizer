package com.redisdockerizer.keymanagement.keymanagement.dto;

import java.util.Set;

/**
 * Represents the response containing details about all active sessions.
 * This record is used to provide information about currently active sessions
 * and their total count.
 * <p>
 * Fields:
 * - activeSessions: A set of session identifiers representing active sessions.
 * - count: The total number of active sessions.
 * <p>
 * This response is typically returned by API endpoints that query for all
 * active session details, such as the `/active` endpoint in the `SessionController`.
 */
public record ActiveSessionsResponse(
        Set<String> activeSessions,
        int count
) {
}

