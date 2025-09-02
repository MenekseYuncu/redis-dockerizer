package com.redisdockerizer.pubsub.pubsub.model;

/**
 * Represents a request to publish a message to a specific channel in a Pub/Sub system.
 * This class encapsulates the essential details required to publish a message, including:
 * <p>
 * - channel: The name of the channel where the message should be published.
 * - user: The identifier of the user who is sending the message.
 * - text: The text content of the message to be published.
 * <p>
 * This record is intended to be used as a data transfer object (DTO) for facilitating
 * the publication of messages via a Pub/Sub system.
 */
public record PublishMessageRequest(
        String channel,
        String user,
        String text
) {

}
