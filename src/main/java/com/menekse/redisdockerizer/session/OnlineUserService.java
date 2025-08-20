package com.menekse.redisdockerizer.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service to manage online/offline status of users using Redis.
 * <p>
 * Features:
 * <ul>
 *     <li>Mark users online/offline</li>
 *     <li>Refresh online TTL</li>
 *     <li>Track last active time</li>
 *     <li>Retrieve all online users</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class OnlineUserService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Time-to-live (TTL) in seconds for online status
     */
    private final long ONLINE_TTL = 30;

    /**
     * Generates the Redis key for the online status of a user.
     *
     * @param userId the user's ID
     * @return Redis key string
     */
    private String keyForUser(String userId) {
        return "user:" + userId + ":online";
    }

    /**
     * Generates the Redis key for storing the last active time of a user.
     *
     * @param userId the user's ID
     * @return Redis key string
     */
    private String lastActiveKey(String userId) {
        return "user:" + userId + ":lastActive";
    }

    /**
     * Marks a user as online and updates their last active time.
     * TTL is applied to online status, lastActive is stored indefinitely.
     *
     * @param userId the user's ID
     */
    public void login(String userId) {
        redisTemplate.opsForValue().set(keyForUser(userId), "ONLINE", ONLINE_TTL, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(lastActiveKey(userId), Instant.now().toString());
    }

    /**
     * Marks a user as offline and updates their last active time.
     * Online key is deleted, lastActive is kept.
     *
     * @param userId the user's ID
     */
    public void logout(String userId) {
        redisTemplate.delete(keyForUser(userId));
        redisTemplate.opsForValue().set(lastActiveKey(userId), Instant.now().toString());
    }

    /**
     * Refreshes the TTL for a user's online status to keep them online.
     * Also updates the last active time.
     *
     * @param userId the user's ID
     */
    public void refresh(String userId) {
        if (redisTemplate.hasKey(keyForUser(userId))) {
            redisTemplate.expire(keyForUser(userId), ONLINE_TTL, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(lastActiveKey(userId), Instant.now().toString());
        }
    }

    /**
     * Checks whether a user is currently online.
     *
     * @param userId the user's ID
     * @return true if the user is online, false otherwise
     */
    public boolean isOnline(String userId) {
        return redisTemplate.hasKey(keyForUser(userId));
    }

    /**
     * Retrieves the last active time of a user.
     * Returns null if the user has never been active.
     *
     * @param userId the user's ID
     * @return ISO timestamp string of last active time, or null
     */
    public String getLastActiveTime(String userId) {
        return redisTemplate.opsForValue().get(lastActiveKey(userId));
    }

    /**
     * Retrieves a set of all current online users.
     *
     * @return set of user IDs currently online
     */
    public Set<String> getOnlineUsers() {
        Set<String> keys = redisTemplate.keys("user:*:online");
        return keys.stream()
                .map(k -> k.replace("user:", "").replace(":online", ""))
                .collect(Collectors.toSet());
    }
}