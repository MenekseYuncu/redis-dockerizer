package com.redisdockerizer.sessionmanagement.session.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a response object for user session-related actions. This class
 * encapsulates information such as message, user details, session status, and
 * optional time-to-live (TTL) duration for the session.
 * <p>
 * This class is typically used to construct standardized responses for session
 * management functionalities.
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse {
    private String message;
    private String userId;
    private String username;
    private String status;
    private Long ttlSeconds;


    /**
     * Creates and returns a new SessionResponse instance with the provided message, userId, username, and status.
     *
     * @param message  the message to be associated with the session response
     * @param userId   the unique identifier of the user
     * @param username the username of the user
     * @param status   the status of the session or user
     * @return a SessionResponse instance containing the provided fields
     */
    public static SessionResponse userAction(String message, String userId, String username, String status) {
        return SessionResponse.builder()
                .message(message)
                .userId(userId)
                .username(username)
                .status(status)
                .build();
    }

    /**
     * Creates a new {@code SessionResponse} instance with the provided details, including
     * a time-to-live (TTL) duration in seconds.
     *
     * @param message    the message associated with the user's action
     * @param userId     the unique identifier of the user
     * @param username   the name of the user performing the action
     * @param status     the status of the user's action
     * @param ttlSeconds the TTL duration in seconds for the session
     * @return a {@code SessionResponse} instance containing the message, user ID, username,
     * status, and TTL details
     */
    public static SessionResponse userActionWithTtl(String message, String userId, String username, String status, Long ttlSeconds) {
        return SessionResponse.builder()
                .message(message)
                .userId(userId)
                .username(username)
                .status(status)
                .ttlSeconds(ttlSeconds)
                .build();
    }
}
