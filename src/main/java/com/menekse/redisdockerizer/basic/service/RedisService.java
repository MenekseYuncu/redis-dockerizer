package com.menekse.redisdockerizer.basic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service class for interacting with Redis.
 * This class provides methods for basic Redis operations such as setting, getting, deleting keys,
 * retrieving all keys, and setting expiration (TTL) for keys.
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Stores the given key-value pair in Redis.
     * If the key already exists, it will be overwritten with the new value.
     *
     * @param key   The key to be stored in Redis.
     * @param value The value associated with the given key.
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Retrieves the value associated with the given key from Redis.
     * If the key does not exist, it will return null.
     *
     * @param key The key whose value is to be retrieved from Redis.
     * @return The value associated with the specified key, or null if the key doesn't exist.
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Deletes the specified key from Redis.
     *
     * @param key The key to be deleted from Redis.
     * @return A boolean value indicating whether the key was successfully deleted.
     * Returns true if the key was deleted, or false if the key does not exist.
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * Retrieves all the keys stored in Redis.
     * ⚠ This operation can be costly and should only be used for debugging or testing.
     * Avoid using the KEYS command in production environments as it can impact performance.
     *
     * @return A set of all keys currently stored in Redis.
     */
    public Set<String> getAllKeys() {
        return redisTemplate.keys("*");
    }

    /**
     * Sets the expiration time (TTL) for a given key in Redis.
     * The TTL determines how long the key will exist before it is automatically deleted.
     *
     * @param key     The key for which the TTL is to be set.
     * @param seconds The expiration time in seconds.
     * @return A boolean value indicating whether the TTL was successfully set.
     * Returns true if the TTL was set successfully, or false if the key does not exist.
     */
    public Boolean expire(String key, long seconds) {
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }
}