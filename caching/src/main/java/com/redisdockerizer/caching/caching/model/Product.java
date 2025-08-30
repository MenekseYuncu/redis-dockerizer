package com.redisdockerizer.caching.caching.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a product model used in the application.
 * This class encapsulates the basic attributes of a product entity,
 * including its unique identifier, name, category, price, and description.
 * <p>
 * Implements {@code Serializable} for object serialization, ensuring compatibility
 * for use cases such as caching or distributed systems.
 * <p>
 * Features:
 * - Automatically provides getters, setters, a default constructor, and an all-args constructor
 *   through Lombok annotations (@Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor).
 * - Overrides {@code toString()} for a string representation of the object for debugging or logging.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    @Serial
    private static final long serialVersionUID = -1115486003719601418L;

    private UUID id;
    private String name;
    private String category;
    private BigDecimal price;
    private String description;

}
