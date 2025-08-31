package com.redisdockerizer.keymanagement.keymanagement.dto;

import java.util.Map;

/**
 * Represents a generic response message with additional data.
 * This record encapsulates a response message and an optional
 * map of additional data that may accompany the message.
 * <p>
 * Typical use cases include sending structured responses from an API
 * that include both a descriptive message and supplementary data
 * related to the operation performed.
 * <p>
 * Fields:
 * - message: A string representing the primary response message.
 * - data: A map containing additional key-value data associated
 *   with the response, useful for providing additional context
 *   or information.
 */
public record MessageResponse(
        String message,
        Map<String, Object> data
) {}
