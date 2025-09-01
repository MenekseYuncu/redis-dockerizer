package com.redisdockerizer.sessionmanagement.session.service;

import com.redisdockerizer.sessionmanagement.session.dto.SessionResponse;
import com.redisdockerizer.sessionmanagement.session.dto.SessionStatsResponse;
import com.redisdockerizer.sessionmanagement.session.repository.UserRepository;
import com.redisdockerizer.sessionmanagement.session.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * The SessionService class provides methods to manage user sessions, including
 * retrieving online and offline users, setting user statuses, managing session TTLs,
 * and tracking session activity using a Redis-backed store.
 * All interactions are logged for debugging and monitoring purposes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final String ONLINE_USERS_SET = "online_users";
    private static final String SESSION_KEY_PREFIX = "session:user:";
    private static final Duration USER_SESSION_TTL = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;


    /**
     * Retrieves a list of all users from the user repository.
     *
     * @return a list containing all users available in the data source
     */
    public List<User> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .toList();
    }

    /**
     * Retrieves a list of online users by validating their session data.
     * The method checks active user IDs, cleans up expired sessions, and fetches
     * corresponding user entities from the data source.
     *
     * @return a list of users who are currently online
     */
    public List<User> getOnlineUsers() {
        Set<String> aliveIds = resolveAliveUserIdsAndCleanup();

        if (aliveIds.isEmpty()) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(userRepository.findAllById(aliveIds).spliterator(), false)
                .toList();
    }

    /**
     * Retrieves a list of users who are currently offline.
     * The method determines online users by resolving active user IDs
     * and filters out the online users from the complete list of users.
     *
     * @return a list of users who are not currently online
     */
    public List<User> getOfflineUsers() {
        Set<String> aliveIds = resolveAliveUserIdsAndCleanup();
        List<User> allUsers = getAllUsers();

        if (aliveIds.isEmpty()) {
            return allUsers;
        }

        return allUsers.stream()
                .filter(user -> !aliveIds.contains(user.getUserId()))
                .toList();
    }

    /**
     * Retrieves session statistics including the total number of users and the count of current online users.
     *
     * @return a {@link SessionStatsResponse} object containing the total number of users
     * and the number of users who are currently online
     */
    public SessionStatsResponse getSessionStats() {
        long totalUsers = userRepository.count();
        long onlineCount = resolveAliveUserIdsAndCleanup().size();

        return SessionStatsResponse.of(totalUsers, onlineCount);
    }


    /**
     * Sets the specified user to online status. This involves updating the
     * user's session data and marking them as online in the user repository.
     *
     * @param userId the unique identifier of the user to be set as online
     * @return a {@link SessionResponse} containing details about the user's
     * online status, including a message, user ID, username, status,
     * and the session TTL in seconds
     */
    @Transactional
    public SessionResponse setUserOnline(String userId) {
        User user = getUserOrThrow(userId);

        upsertSession(userId);
        updateUserOnlineStatus(user, true);

        return SessionResponse.userActionWithTtl(
                "User set to online successfully",
                userId,
                user.getUsername(),
                "online",
                USER_SESSION_TTL.toSeconds()
        );
    }

    /**
     * Sets a user's status to offline by removing their session and updating their online status.
     *
     * @param userId the unique identifier of the user to be set offline
     * @return a {@link SessionResponse} containing details of the action performed, including a message, user ID, username, and status
     */
    @Transactional
    public SessionResponse setUserOffline(String userId) {
        User user = getUserOrThrow(userId);

        removeSession(userId);
        updateUserOnlineStatus(user, false);

        return SessionResponse.userAction(
                "User set to offline successfully",
                userId,
                user.getUsername(),
                "offline"
        );
    }

    /**
     * Removes a user from the system by deleting their session and user record from the repository.
     *
     * @param userId the unique identifier of the user to be removed
     * @return a {@link SessionResponse} containing details of the action performed, including a message, user ID, username, and status
     */
    @Transactional
    public SessionResponse removeUser(String userId) {
        User user = getUserOrThrow(userId);
        String username = user.getUsername();

        removeSession(userId);
        userRepository.deleteById(userId);

        return SessionResponse.userAction(
                "User removed successfully",
                userId,
                username,
                "removed"
        );
    }

    /**
     * Refreshes the TTL (Time-To-Live) for the session of a specified user.
     * This method ensures the session remains active and updates the TTL in the backend.
     *
     * @param userId the unique identifier of the user whose session TTL is to be refreshed
     */
    public SessionResponse refreshUserTtl(String userId) {
        User user = getUserOrThrow(userId);

        upsertSession(userId);
        Long ttl = getTtlSeconds(userId);

        return SessionResponse.userActionWithTtl(
                "User session TTL refreshed successfully",
                userId,
                user.getUsername(),
                "refreshed",
                ttl
        );
    }

    /**
     * Retrieves a user by their unique identifier or throws an exception if the user is not found.
     * The method fetches the user from the repository and logs an error if the user does not exist.
     *
     * @param userId the unique identifier of the user to be retrieved
     * @return the {@link User} object corresponding to the provided userId
     * @throws NoSuchElementException if the user with the specified userId is not found
     */
    private User getUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new NoSuchElementException("User not found: " + userId);
                });
    }

    /**
     * Updates the online status of a given user and persists the change in the repository.
     *
     * @param user     the user whose online status is being updated
     * @param isOnline a boolean indicating the online status to set; true for online, false for offline
     */
    private void updateUserOnlineStatus(User user, boolean isOnline) {
        user.setIsOnline(isOnline);
        userRepository.save(user);
    }

    /**
     * Generates a session key for the given user ID by appending the user ID
     * to a predefined session key prefix.
     *
     * @param userId the unique identifier of the user for whom the session key is being generated
     * @return the generated session key as a concatenation of the session key prefix and user ID
     */
    private String sessionKey(String userId) {
        return SESSION_KEY_PREFIX + userId;
    }

    /**
     * Checks if a session for the given user ID is active.
     * The method verifies the existence of a session key for the specified user
     * in the Redis data store.
     *
     * @param userId the unique identifier of the user whose session is to be checked
     * @return true if the session key exists, indicating the session is active; false otherwise
     */
    private boolean isSessionAlive(String userId) {
        return redisTemplate.hasKey(sessionKey(userId));
    }

    /**
     * Retrieves the Time-to-Live (TTL) in seconds for a user's session.
     *
     * @param userId the unique identifier of the user whose session TTL is being retrieved
     * @return the TTL in seconds; if the TTL is negative or undefined, returns a default session TTL
     */
    private Long getTtlSeconds(String userId) {
        Long ttl = redisTemplate.getExpire(sessionKey(userId));
        return ttl >= 0 ? ttl : USER_SESSION_TTL.toSeconds();
    }

    /**
     * Validates online users against their TTL and cleans up expired sessions.
     * Returns only active user IDs.
     */
    private Set<String> resolveAliveUserIdsAndCleanup() {
        Set<String> members = redisTemplate.opsForSet().members(ONLINE_USERS_SET);

        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }

        List<String> expiredIds = new ArrayList<>();
        Set<String> aliveIds = new HashSet<>();

        for (String userId : members) {
            if (isSessionAlive(userId)) {
                aliveIds.add(userId);
            } else {
                expiredIds.add(userId);
            }
        }

        if (!expiredIds.isEmpty()) {
            cleanupExpiredSessions(expiredIds);
        }

        return aliveIds;
    }

    /**
     * Cleans up expired user sessions by removing their identifiers from the online users set.
     *
     * @param expiredIds a list of session identifiers that have expired and need to be removed
     */
    private void cleanupExpiredSessions(List<String> expiredIds) {
        if (!expiredIds.isEmpty()) {
            redisTemplate.opsForSet().remove(ONLINE_USERS_SET, expiredIds.toArray());
            // toArray() with no args returns Object[], which matches Object... correctly
        }
    }


    /**
     * Adds a user to an online set and creates/refreshes a TTL key.
     */
    private void upsertSession(String userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_SET, userId);
        redisTemplate.opsForValue().set(sessionKey(userId), "online", USER_SESSION_TTL);
        log.debug("Session upserted for user {} with TTL {} seconds", userId, USER_SESSION_TTL.toSeconds());
    }

    /**
     * Removes a user from an online set and deletes a TTL key.
     */
    private void removeSession(String userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_SET, userId);
        redisTemplate.delete(sessionKey(userId));
        log.debug("Session removed for user {}", userId);
    }
}