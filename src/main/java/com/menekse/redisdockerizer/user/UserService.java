package com.menekse.redisdockerizer.user;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage application users.
 * <p>
 * This service initializes a set of default users and stores them in memory.
 * Can be extended later for CRUD operations or database integration.
 * </p>
 */
@Service
public class UserService {

    /**
     * In-memory storage for users, thread-safe.
     * Key: userId
     * Value: UserEntity object
     */
    private final Map<String, UserEntity> userMap = new ConcurrentHashMap<>();

    /**
     * Initializes 3 default users when the application starts.
     * Users:
     * <ul>
     *     <li>1 - Alice</li>
     *     <li>2 - Bob</li>
     *     <li>3 - Charlie</li>
     * </ul>
     */
    @PostConstruct
    public void init() {
        userMap.put("1", new UserEntity("1", "alice", "alice@example.com"));
        userMap.put("2", new UserEntity("2", "bob", "bob@example.com"));
        userMap.put("3", new UserEntity("3", "charlie", "charlie@example.com"));
    }
}