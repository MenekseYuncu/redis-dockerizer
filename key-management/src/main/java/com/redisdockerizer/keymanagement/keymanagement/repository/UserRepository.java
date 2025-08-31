package com.redisdockerizer.keymanagement.keymanagement.repository;


import com.redisdockerizer.keymanagement.keymanagement.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository class for managing user data and providing methods to interact with user information.
 * The class contains an in-memory store of users and provides functionality for user retrieval and credential validation.
 * It initializes mock data for demonstration purposes.
 */
@Repository
@Slf4j
public class UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, User> usersByUsername = new HashMap<>();

    /**
     * Constructor for the UserRepository class.
     * Initializes the repository with mock user data for demonstration purposes.
     * This setup populates an in-memory store with predefined user information,
     * including user IDs, usernames, passwords, email addresses, roles, and active statuses.
     */
    public UserRepository() {
        initializeMockData();
    }

    /**
     * Initializes in-memory mock user data for demonstration purposes.
     * The method creates and stores predefined user objects with properties such as ID, username, password, email, roles, and active status.
     * It populates two internal data structures:
     * - A map with user IDs as keys to store user objects.
     * - A map with usernames as keys to facilitate user lookup by username.
     * <p>
     * The predefined users include both active and inactive accounts, as well as users with varying roles.
     */
    private void initializeMockData() {
        User admin = new User(1L,
                "admin",
                "admin123",
                "admin@example.com",
                Arrays.asList("ADMIN", "USER"),
                true
        );
        User user1 = new User(2L,
                "john_doe",
                "password123",
                "john@example.com",
                List.of("USER"),
                true
        );
        User user2 = new User(3L,
                "jane_smith",
                "password456",
                "jane@example.com",
                Arrays.asList("USER", "MANAGER"),
                true
        );
        User user3 = new User(4L,
                "disabled_user",
                "password789",
                "disabled@example.com",
                List.of("USER"),
                false
        );

        users.put(1L, admin);
        users.put(2L, user1);
        users.put(3L, user2);
        users.put(4L, user3);

        usersByUsername.put("admin", admin);
        usersByUsername.put("john_doe", user1);
        usersByUsername.put("jane_smith", user2);
        usersByUsername.put("disabled_user", user3);
    }

    /**
     * Finds a user by their username.
     * This method retrieves a user from the in-memory user store based on the provided username.
     * If no user is found with the specified username, an empty {@code Optional} is returned.
     *
     * @param username the username of the user to be retrieved
     * @return an {@code Optional} containing the user if found, or an empty {@code Optional} if no user exists with the given username
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    /**
     * Validates user credentials by checking if the provided username exists,
     * if the user is active, and if the provided password matches the stored password.
     *
     * @param username the username of the user attempting to authenticate
     * @param password the password provided for authentication
     * @return {@code true} if the credentials are valid (user exists, is active, and password matches);
     *         {@code false} otherwise
     */
    public boolean validateCredentials(String username, String password) {
        User user = usersByUsername.get(username);
        return user != null && user.isActive() && user.getPassword().equals(password);
    }
}
