package com.integration.redisdockerizer.session.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service to manage the online/offline status of users using Redis.
 * <p>This service provides operations to track users' online status and activity. It uses Redis to store
 * information about whether a user is currently online and their last active time. The online status is managed
 * with a Time-To-Live (TTL), so users are automatically marked offline after a specified period of inactivity.</p>
 *
 * <p><b>Features:</b></p>
 * <ul>
 *     <li><b>Mark users online/offline:</b> Allows marking users as online or offline based on their activity.</li>
 *     <li><b>Refresh online TTL:</b> Resets the TTL for a user’s online status to keep them online as long as they remain active.</li>
 *     <li><b>Track last active time:</b> Keeps track of the timestamp for each user's last activity.</li>
 *     <li><b>Retrieve all online users:</b> Returns a list of all users who are currently marked as online.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Time-to-live (TTL) in seconds for online status.
     * After this period, the user's online status will expire, and they will be marked offline automatically.
     */
    private static final long ONLINE_TTL = 30;

    /**
     * Generates the Redis key used to store the online status of a user.
     *
     * @param userId the ID of the user
     * @return the Redis key string for the user's online status (e.g., "user:123:online")
     */
    private String keyForUser(String userId) {
        return "user:" + userId + ":online";
    }

    /**
     * Generates the Redis key used to store the last active timestamp of a user.
     *
     * @param userId the ID of the user
     * @return the Redis key string for the user's last active timestamp (e.g., "user: 123: lastActive")
     */
    private String lastActiveKey(String userId) {
        return "user:" + userId + ":lastActive";
    }

    /**
     * Marks a user as online by setting their online status in Redis with a TTL.
     * Also updates the user's last active time to the current timestamp.
     * <p>
     * The online status key is set with a TTL, so after the TTL expires, the user will be automatically marked offline.
     * The last active time is stored indefinitely, providing a record of the user's activity.
     *
     * @param userId the ID of the user to mark as online
     */
    public void login(String userId) {
        redisTemplate.opsForValue().set(keyForUser(userId), "ONLINE", ONLINE_TTL, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(lastActiveKey(userId), Instant.now().toString());
    }

    /**
     * Marks a user as offline by deleting their online status key and updating their last active time.
     * The online status is deleted from Redis, while the last active time remains in the database.
     *
     * @param userId the ID of the user to mark as offline
     */
    public void logout(String userId) {
        redisTemplate.delete(keyForUser(userId));
        redisTemplate.opsForValue().set(lastActiveKey(userId), Instant.now().toString());
    }

    /**
     * Refreshes the TTL for a user's online status, effectively keeping them online as long as they remain active.
     * The last active time is also updated to the current timestamp.
     * This method ensures that a user remains online as long as they are actively engaging with the system.
     *
     * @param userId the ID of the user whose online status TTL needs to be refreshed
     */
    public void refresh(String userId) {
        if (redisTemplate.hasKey(keyForUser(userId))) {
            redisTemplate.expire(keyForUser(userId), ONLINE_TTL, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(lastActiveKey(userId), Instant.now().toString());
        }
    }

    /**
     * Checks whether a user is currently online by looking for their online status in Redis.
     * If the user is found in Redis with a valid online status, they are considered online.
     *
     * @param userId the ID of the user to check
     * @return true if the user is online, false otherwise
     */
    public boolean isOnline(String userId) {
        return redisTemplate.hasKey(keyForUser(userId));
    }

    /**
     * Retrieves the last active time of a user, which is stored as an ISO timestamp in Redis.
     * Returns null if the user has never been active or does not have the last active time recorded.
     *
     * @param userId the ID of the user whose last active time needs to be retrieved
     * @return an ISO 8601 formatted timestamp string of the last active time, or null if the user has no record
     */
    public String getLastActiveTime(String userId) {
        return redisTemplate.opsForValue().get(lastActiveKey(userId));
    }

    /**
     * Retrieves the set of all currently online users by querying Redis for all keys matching the online status pattern.
     * The keys are filtered to extract the user IDs, and a set of these IDs is returned.
     *
     * @return a set of user IDs currently marked as online
     */
    public Set<String> getOnlineUsers() {
        Set<String> keys = redisTemplate.keys("user:*:online");
        return keys.stream()
                .map(k -> k.replace("user:", "").replace(":online", ""))
                .collect(Collectors.toSet());
    }
}