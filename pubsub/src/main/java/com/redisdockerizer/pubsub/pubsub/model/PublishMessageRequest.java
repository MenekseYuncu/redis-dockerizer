package com.redisdockerizer.pubsub.pubsub.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a request object for publishing a message in a Pub/Sub system.
 * This class encapsulates the necessary data required to publish a message to a specific channel
 * by a specific user along with the message content.
 * <p>
 * Fields:
 * - channel: Specifies the target channel where the message will be published.
 * - user: Identifies the user initiating the message publication.
 * - Text: Contains the actual message content to be published.
 */
@Getter
@Setter
public class PublishMessageRequest {

    private String channel;
    private String user;
    private String text;

}
