package com.redisdockerizer.sessionmanagement.session.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user in the system.
 * <p>
 * This entity holds keymanagement information about a user including:
 * <ul>
 *     <li>id: unique identifier</li>
 *     <li>username: login name</li>
 *     <li>email: user's email address</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    /**
     * Unique identifier for the user
     */
    private String id;

    /**
     * Username used for login or display
     */
    private String username;

    /**
     * Email address of the user
     */
    private String email;
}