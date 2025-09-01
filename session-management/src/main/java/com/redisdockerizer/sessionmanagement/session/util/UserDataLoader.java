package com.redisdockerizer.sessionmanagement.session.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisdockerizer.sessionmanagement.session.repository.UserRepository;
import com.redisdockerizer.sessionmanagement.session.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The UserDataLoader class is responsible for loading user data into the system during
 * the application startup. It implements the CommandLineRunner interface, ensuring its
 * execution after the Spring Boot application context has fully initialized.
 * <p>
 * This loader performs actions such as clearing existing user data, reading new data
 * from a file, saving the data into the database, and maintaining online users in the
 * Redis datastore.
 * <p>
 * Primary responsibilities include:
 * 1. Clearing all existing user data from the database and Redis store.
 * 2. Reading and parsing user data from a JSON file (`user.json`).
 * 3. Populating the database with the parsed user data.
 * 4. Updating the Redis set to reflect the IDs of users who are currently marked as online.
 * <p>
 * It handles error scenarios such as
 * - Missing or inaccessible `user.json` file.
 * - Exceptions during file processing or data persistence.
 * - Unexpected runtime errors.
 * <p>
 * The loadUsersFromJson method encapsulates the details of this process, handling user
 * data retrieval, database updates, and Redis set updates, along with appropriate logging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataLoader implements CommandLineRunner {

    private static final String ONLINE_USERS_SET = "online_users";

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Executes the process of loading user data from a JSON file at application startup.
     * This method is part of the `CommandLineRunner` implementation and is invoked
     * automatically once the application context is fully initialized.
     *
     * @param args optional arguments passed during application startup, which are
     *             not used in the current implementation.
     */
    @Override
    public void run(String... args) {
        loadUsersFromJson();
    }

    /**
     * Loads user data from the `user.json` file in the classpath and performs the following steps:
     * <ul>
     *    <li>Deletes all entries from the online users Redis set.</li>
     *    <li>Clears all user data from the database.</li>
     *    <li>Reads the user data from `user.json` into a list of User objects.</li>
     *    <li>Saves all user data into the database.</li>
     *    <li>Identifies online users based on the `isOnline` field and updates the Redis set with their user IDs.</li>
     * </ul>
     * Logs the number of total users loaded, the number of online users, and the number of offline users.
     * <p>
     * Handles the following scenarios:
     * 1. Logs an error if the `user.json` file is not found.
     * 2. Logs an error if an issue occurs while reading the file or processing data.
     * 3. Logs details if an unexpected exception occurs during execution.
     */
    private void loadUsersFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("data/user.json");

            if (!resource.exists()) {
                log.error("UserDataLoader: user.json file not found in resources/data.");
                return;
            }
            try (InputStream inputStream = resource.getInputStream()) {
                List<User> users = objectMapper.readValue(
                        inputStream,
                        new TypeReference<>() {
                        }
                );

                stringRedisTemplate.delete(ONLINE_USERS_SET);
                userRepository.deleteAll();

                userRepository.saveAll(users);

                List<String> onlineUserIds = users.stream()
                        .filter(user -> Boolean.TRUE.equals(user.getIsOnline()))
                        .map(User::getUserId)
                        .toList();

                if (!onlineUserIds.isEmpty()) {
                    stringRedisTemplate.opsForSet().add(ONLINE_USERS_SET, onlineUserIds.toArray(new String[0]));
                }

                log.info("UserDataLoader: {} users loaded ({} online, {} offline).",
                        users.size(), onlineUserIds.size(), users.size() - onlineUserIds.size());
            }

        } catch (IOException e) {
            log.error("UserDataLoader: Error while reading user.json: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("UserDataLoader: Unexpected error occurred: {}", e.getMessage(), e);
        }
    }
}
