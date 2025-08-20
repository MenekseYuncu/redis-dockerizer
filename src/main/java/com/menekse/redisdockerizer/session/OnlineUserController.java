package com.menekse.redisdockerizer.session;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST controller for managing online/offline user sessions.
 * <p>
 * Provides endpoints for:
 * <ul>
 *     <li>Logging in and marking users online</li>
 *     <li>Logging out and marking users offline</li>
 *     <li>Refreshing TTL to keep users online</li>
 *     <li>Checking user online status and last active time</li>
 *     <li>Retrieving all current online users</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class OnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * Logs in a user and marks them as online.
     *
     * @param userId the user's ID
     * @return confirmation message
     */
    @PostMapping("/login/{userId}")
    public ResponseEntity<String> login(@PathVariable String userId) {
        onlineUserService.login(userId);
        return ResponseEntity.ok("User " + userId + " is now ONLINE");
    }

    /**
     * Logs out a user and marks them as offline.
     *
     * @param userId the user's ID
     * @return confirmation message
     */
    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable String userId) {
        onlineUserService.logout(userId);
        return ResponseEntity.ok("User " + userId + " is now OFFLINE");
    }

    /**
     * Refreshes the user's online TTL to keep them online.
     *
     * @param userId the user's ID
     * @return confirmation message
     */
    @PostMapping("/refresh/{userId}")
    public ResponseEntity<String> refresh(@PathVariable String userId) {
        onlineUserService.refresh(userId);
        return ResponseEntity.ok("User " + userId + " TTL refreshed");
    }

    /**
     * Retrieves the online status and last active time of a specific user.
     *
     * @param userId the user's ID
     * @return string indicating online status and last active time
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<String> getUserStatus(@PathVariable String userId) {
        boolean online = onlineUserService.isOnline(userId);
        String lastActive = onlineUserService.getLastActiveTime(userId);
        return ResponseEntity.ok("User " + userId + " online? " + online + " | Last Active: " + lastActive);
    }

    /**
     * Retrieves a set of all current online users.
     *
     * @return set of user IDs currently online
     */
    @GetMapping("/online")
    public ResponseEntity<Set<String>> getOnlineUsers() {
        Set<String> users = onlineUserService.getOnlineUsers();
        return ResponseEntity.ok(users);
    }
}