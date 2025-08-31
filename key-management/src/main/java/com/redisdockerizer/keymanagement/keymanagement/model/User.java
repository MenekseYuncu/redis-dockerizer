package com.redisdockerizer.keymanagement.keymanagement.model;

import lombok.*;

import java.util.List;

/**
 * The User class represents a user entity in the system.
 * It encapsulates the basic information and properties associated with a user,
 * such as their unique identifier, username, credentials, email, roles, and active status.
 * <p>
 * This class provides a foundational model for managing user data, typically used for
 * authentication, authorization, and user management operations.
 * <p>
 * Attributes:
 * - id: Unique identifier for the user.
 * - username: The user's login name or identifier.
 * - password: The user's encrypted password for authentication purposes.
 * - email: The user's contact email address.
 * - roles: A list of roles assigned to the user, determining their access and permissions.
 * - active: A flag indicating whether the user's account is active or inactive.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private List<String> roles;
    private boolean active;

}
