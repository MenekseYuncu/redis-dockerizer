package com.menekse.redisdockerizer.session.controller;

import com.menekse.redisdockerizer.session.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST controller for managing user sessions, specifically tracking the online and offline status of users.
 * <p>This controller provides various endpoints to manage and track the online status of users, including logging in,
 * logging out, refreshing the online session's TTL (Time-To-Live), retrieving a user's online status and last active time,
 * and fetching all currently online users.</p>
 *
 * <p><b>Available operations:</b></p>
 * <ul>
 *     <li><b>POST /api/session/login/{userId}</b> → Log in a user and mark them as online.</li>
 *     <li><b>POST /api/session/logout/{userId}</b> → Log out a user and mark them as offline.</li>
 *     <li><b>POST /api/session/refresh/{userId}</b> → Refresh the user's online TTL to keep them online.</li>
 *     <li><b>GET /api/session/status/{userId}</b> → Retrieve the online status and last active time of a user.</li>
 *     <li><b>GET /api/session/online</b> → Retrieve the list of all currently online users.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * Logs in a user and marks them as online.
     * This endpoint sets a TTL for the user’s online status, after which they will be marked offline automatically.
     *
     * @param userId the ID of the user to log in
     * @return a confirmation message indicating the user is now online
     */
    @PostMapping("/login/{userId}")
    public ResponseEntity<String> login(@PathVariable String userId) {
        onlineUserService.login(userId);
        return ResponseEntity.ok("User " + userId + " is now ONLINE");
    }

    /**
     * Logs out a user and marks them as offline.
     * This removes the user's online status and keeps the last active timestamp for future reference.
     *
     * @param userId the ID of the user to log out
     * @return a confirmation message indicating the user is now offline
     */
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable String userId) {
        onlineUserService.logout(userId);
        return ResponseEntity.ok("User " + userId + " is now OFFLINE");
    }

    /**
     * Refreshes the TTL for a user's online status to keep them marked as online.
     * This ensures that a user remains online as long as they continue to interact with the system.
     *
     * @param userId the ID of the user whose TTL needs to be refreshed
     * @return a confirmation message indicating that the user's online session TTL has been refreshed
     */
    @PostMapping("/refresh/{userId}")
    public ResponseEntity<String> refresh(@PathVariable String userId) {
        onlineUserService.refresh(userId);
        return ResponseEntity.ok("User " + userId + " TTL refreshed");
    }

    /**
     * Retrieves the online status and the last active time of a specific user.
     * If the user is online, only the online status is returned. If the user is offline, both the status and
     * the last active time are provided.
     *
     * @param userId the ID of the user whose status and last active time are to be retrieved
     * @return a string message indicating the user's online status and, if offline, their last active time
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<String> getUserStatus(@PathVariable String userId) {
        boolean online = onlineUserService.isOnline(userId);
        String responseMessage = "User " + userId + " online? " + online;

        if (!online) {
            String lastActive = onlineUserService.getLastActiveTime(userId);
            responseMessage += " | Last Active: " + lastActive;
        }
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Retrieves a set of all current online users.
     * This endpoint provides a list of user IDs who are currently marked as online in the system.
     *
     * @return a set of user IDs that are currently online
     */
    @GetMapping("/online")
    public ResponseEntity<Set<String>> getOnlineUsers() {
        Set<String> users = onlineUserService.getOnlineUsers();
        return ResponseEntity.ok(users);
    }
}