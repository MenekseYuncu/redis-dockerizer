package com.redisdockerizer.keymanagement.keymanagement.dto;

/**
 * Represents the response for terminating all active sessions of a specific user.
 * This record encapsulates the operation's outcome, including a status message,
 * the user identifier, and the number of sessions that were terminated.
 * <p>
 * Fields:
 * - message: A descriptive message indicating the result of the termination operation.
 * - userId: The identifier of the user whose active sessions were terminated.
 * - terminatedCount: The number of sessions successfully terminated for the user.
 * <p>
 * This response is typically returned by the API endpoint responsible for terminating
 * all active sessions for a user, providing feedback on the operation's success and details.
 */
public record TerminateAllResponse(
        String message,
        Long userId,
        int terminatedCount
) {
}

