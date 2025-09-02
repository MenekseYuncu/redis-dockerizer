package com.redisdockerizer.pubsub.pubsub.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;

/**
 * Represents a message entity used in a Pub/Sub system.
 * This class models the essential properties of a message, including its unique identifier,
 * associated user, room, content, type, timestamp, status, and last modification time.
 * <p>
 * Key fields:
 * - id: Unique identifier of the message.
 * - user: Identifier for the user associated with the message.
 * - room: Identifier for the room or context the message belongs to.
 * - text: The actual content of the message.
 * - type: Specifies the type or category of the message.
 * - timestamp: The creation time of the message, formatted as a string.
 * - status: The current status of the message (e.g., seen, delivered).
 * - lastModified: The last modification timestamp of the message, formatted as a string.
 * <p>
 * This class is annotated for JSON handling and uses Lombok annotations to auto-generate boilerplate code
 * such as getters, setters, constructors, and 'toString' implementations.
 */
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private String id;
    private String user;
    private String room;
    private String text;
    private String type;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant lastModified;

}
