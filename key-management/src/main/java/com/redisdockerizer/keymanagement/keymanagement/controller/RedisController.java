package com.integration.redisdockerizer.keymanagement.controller;

import com.integration.redisdockerizer.keymanagement.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * REST API Controller class for performing operations with Redis.
 * This class provides endpoints to interact with Redis for key-value operations.
 */
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    /**
     * Stores the specified key and value in Redis.
     *
     * @param key   The key to be stored in Redis.
     * @param value The value associated with the key.
     * @return A 200 OK response indicating the key has been successfully set.
     */
    @PostMapping("/set")
    public ResponseEntity<String> set(@RequestParam String key, @RequestParam String value) {
        // Input validation
        if (!StringUtils.hasText(key) || !StringUtils.hasText(value)) {
            return ResponseEntity.badRequest().body("Key and value cannot be empty");
        }
        
        // Key length validation
        if (key.length() > 1000 || value.length() > 10000) {
            return ResponseEntity.badRequest().body("Key or value too long");
        }
        
        redisService.set(key, value);
        return ResponseEntity.ok("Key set successfully: " + key);
    }

    /**
     * Retrieves the value associated with the specified key from Redis.
     *
     * @param key The key whose value is to be retrieved.
     * @return A 200-OK response with the value if the key exists,
     * or a 404 Not Found response if the key does not exist.
     */
    @GetMapping("/get/{key}")
    public ResponseEntity<String> get(@PathVariable String key) {
        // Input validation
        if (!StringUtils.hasText(key)) {
            return ResponseEntity.badRequest().body("Key cannot be empty");
        }
        
        String value = redisService.get(key);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    /**
     * Deletes the specified key from Redis.
     *
     * @param key The key to be deleted.
     * @return A 200-OK response indicating the key has been successfully deleted,
     * or a 404 Not Found response if the key does not exist.
     */
    @DeleteMapping("/del/{key}")
    public ResponseEntity<String> delete(@PathVariable String key) {
        // Input validation
        if (!StringUtils.hasText(key)) {
            return ResponseEntity.badRequest().body("Key cannot be empty");
        }
        
        Boolean deleted = redisService.delete(key);
        return Boolean.TRUE.equals(deleted)
                ? ResponseEntity.ok("Key deleted: " + key)
                : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all keys from Redis.
     * <p>
     * ⚠ This operation can be expensive for large datasets and should only be used for debugging or testing purposes.
     *
     * @return A set of all keys stored in Redis.
     */
    @GetMapping("/keys")
    public ResponseEntity<Set<String>> getAllKeys() {
        return ResponseEntity.ok(redisService.getAllKeys());
    }

    /**
     * Sets a Time-To-Live (TTL) for the specified key in Redis.
     *
     * @param key     The key for which the TTL is to be set.
     * @param seconds The TTL duration in seconds.
     * @return A 200-OK response indicating that the TTL has been successfully set for the key,
     * or a 404 Not Found response if the key does not exist.
     */
    @PostMapping("/expire/{key}")
    public ResponseEntity<String> expire(@PathVariable String key, @RequestParam long seconds) {
        // Input validation
        if (!StringUtils.hasText(key)) {
            return ResponseEntity.badRequest().body("Key cannot be empty");
        }
        
        // TTL validation
        if (seconds <= 0 || seconds > 31536000) { // Max 1 year
            return ResponseEntity.badRequest().body("TTL must be between 1 and 31536000 seconds");
        }
        
        Boolean result = redisService.expire(key, seconds);
        return Boolean.TRUE.equals(result)
                ? ResponseEntity.ok("TTL set for key: " + key + " (" + seconds + "s)")
                : ResponseEntity.notFound().build();
    }
}
