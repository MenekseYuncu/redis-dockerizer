package com.redisdockerizer.sessionmanagement.session.controller;

import com.redisdockerizer.sessionmanagement.session.dto.SessionResponse;
import com.redisdockerizer.sessionmanagement.session.dto.SessionStatsResponse;
import com.redisdockerizer.sessionmanagement.session.service.SessionService;
import com.redisdockerizer.sessionmanagement.session.user.User;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This class provides RESTful API endpoints for managing user sessions and user status within the system.
 * It includes operations to retrieve user information, manage online/offline status, refresh session TTL,
 * and remove users.
 * <p>
 * The controller interacts with the {@code SessionService} to perform all underlying data operations.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;


    /**
     * Retrieves a list of all users in the system.
     *
     * @return a {@code List<User>} containing the details of all users
     */
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return sessionService.getAllUsers();
    }

    /**
     * Retrieves a list of users who are currently online.
     *
     * @return a {@code List<User>} containing information about all online users
     */
    @GetMapping("/sessions/online")
    public List<User> getOnlineUsers() {
        return sessionService.getOnlineUsers();
    }

    /**
     * Retrieves a list of users who are currently offline.
     *
     * @return a {@code List<User>} containing information about all offline users
     */
    @GetMapping("/sessions/offline")
    public List<User> getOfflineUsers() {
        return sessionService.getOfflineUsers();
    }

    /**
     * Retrieves statistical information about user sessions, such as the total number of users,
     * the number of online users, the number of offline users, and the percentage of users currently online.
     *
     * @return a {@code SessionStatsResponse} object containing session-related statistics,
     * including total users, online users, offline users, and the online percentage
     */
    @GetMapping("/sessions/stats")
    public SessionStatsResponse getSessionStats() {
        return sessionService.getSessionStats();
    }


    /**
     * Sets the specified user to online status. This updates the user's session data and marks them
     * as online within the system.
     *
     * @param userId the unique identifier of the user to be set online; must not be blank
     * @return a {@code SessionResponse} containing details about the user's session, including a
     * success message, user information, current status, and session TTL in seconds
     */
    @PostMapping("/sessions/{userId}/online")
    public SessionResponse setUserOnline(
            @PathVariable @NotBlank String userId) {

        return sessionService.setUserOnline(userId);
    }

    /**
     * Sets the specified user's status to offline. This action removes the user's session
     * and updates their online status in the system.
     *
     * @param userId the unique identifier of the user to be set offline; must not be blank
     * @return a {@code SessionResponse} containing details of the operation, such as a message,
     * user ID, username, and the updated status of the user
     */
    @PostMapping("/sessions/{userId}/offline")
    public SessionResponse setUserOffline(
            @PathVariable @NotBlank String userId) {

        return sessionService.setUserOffline(userId);
    }

    /**
     * Refreshes the Time-To-Live (TTL) for the session of the specified user.
     * This operation ensures that the user's session remains active by updating the session's TTL.
     *
     * @param userId the unique identifier of the user whose session TTL is to be refreshed; must not be blank
     * @return a {@code SessionResponse} containing details of the operation, including a success message,
     * user information, updated TTL value, and session status
     */
    @PostMapping("/sessions/{userId}/refresh-ttl")
    public SessionResponse refreshUserTtl(
            @PathVariable @NotBlank String userId) {

        return sessionService.refreshUserTtl(userId);
    }

    /**
     * Removes a user from the system by their unique identifier.
     * This operation deletes the user's session along with their user record.
     *
     * @param userId the unique identifier of the user to be removed must not be blank
     * @return a {@code SessionResponse} containing details of the removal operation,
     * including a message, user ID, username, and the status of the action
     */
    @DeleteMapping("/users/{userId}")
    public SessionResponse removeUser(
            @PathVariable @NotBlank String userId) {

        return sessionService.removeUser(userId);
    }
}