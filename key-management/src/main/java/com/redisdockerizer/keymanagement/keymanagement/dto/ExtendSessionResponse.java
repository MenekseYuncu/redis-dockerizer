package com.redisdockerizer.keymanagement.keymanagement.dto;

/**
 * Represents the response for extending a user session.
 * This record encapsulates information about a session extension operation,
 * including the outcome message, session identifier, and the duration by which the session was extended.
 * <p>
 * Fields:
 * - message: A descriptive message indicating the result of the session extension operation.
 * - sessionId: The unique identifier of the session being extended.
 * - extendedByMinutes: The number of minutes by which the session's expiry was extended.
 * <p>
 * This response is typically returned by the session extension endpoint to provide details
 * about the success and specifics of the session extension.
 */
public record ExtendSessionResponse(
        String message,
        String sessionId,
        int extendedByMinutes
) {}

