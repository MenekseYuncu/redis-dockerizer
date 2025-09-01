package com.redisdockerizer.sessionmanagement.session.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;


/**
 * Represents a User entity stored in Redis with attributes such as user ID, username,
 * email, role, last login timestamp, and online status. This class provides
 * serialization, deserialization, and validation capabilities for its fields.
 * <p>
 * The User entity is primarily used as part of session management interactions,
 * allowing for operations such as querying users, tracking online status, and
 * maintaining session-related attributes.
 * <p>
 * Annotations:
 * - {@code @Getter} and {@code @Setter} for generating getter and setter methods for all fields.
 * - {@code @ToString} for generating a string representation of the object.
 * - {@code @RedisHash} for storing the entity in Redis under the hash name "User".
 * - {@code @AllArgsConstructor} to generate a constructor with all fields.
 * - {@code @NoArgsConstructor} to generate a no-argument constructor.
 * <p>
 */
@Getter
@Setter
@ToString
@RedisHash("User")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /**
     * Represents the unique identifier for the user.
     * This field serves as the primary key for identifying a user entity within the system.
     * It is annotated with {@code @Id} to denote its use as the primary identifier in the
     * Redis data store.
     * <p>
     * Serialization/Deserialization:
     * - Mapped to the JSON property "user_id" during serialization and deserialization
     * to ensure compatibility with external systems that use this naming convention.
     */
    @Id
    @JsonProperty("user_id")
    private String userId;

    /**
     * The username associated with the user.
     * This field cannot be blank and must contain a valid non-empty string.
     * It is primarily used to represent the user's identifiable name in the system.
     * <p>
     * Constraints:
     * - Must not be null or empty.
     */
    @NotBlank
    private String username;

    /**
     * The email address of the user.
     * This field must be a valid email format and cannot be blank.
     * <p>
     * Constraints:
     * - {@code @Email}: Ensures the value adheres to a valid email format.
     * - {@code @NotBlank}: Ensures the field is not null, empty, or comprised solely of whitespace.
     */
    @Email
    @NotBlank
    private String email;

    /**
     * Represents the role assigned to the user in the system.
     * The role must be one of the following values: "admin", "moderator", or "user".
     * It determines the user's level of access and permissions.
     * <p>
     * This field cannot be blank and must match the defined pattern.
     * <p>
     * Validation:
     * - Must be non-blank.
     * - Must match the regular expression "admin|moderator|user".
     */
    @NotBlank
    @Pattern(regexp = "admin|moderator|user")
    private String role;

    /**
     * Represents the timestamp of the user's last successful login.
     * This field is mandatory and cannot be null.
     * Mapped to the JSON property "last_login" for serialization and deserialization.
     */
    @NotNull
    @JsonProperty("last_login")
    private Instant lastLogin;

    /**
     * Represents the online status of a user.
     * This field indicates whether the user is currently online.
     * It is optional and may be null if not explicitly set.
     * <p>
     * Mapped to JSON property "is_online" during serialization and deserialization.
     */
    @JsonProperty("is_online")
    private Boolean isOnline;

}