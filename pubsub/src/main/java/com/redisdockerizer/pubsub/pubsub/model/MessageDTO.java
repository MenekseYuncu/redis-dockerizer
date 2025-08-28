package com.redisdockerizer.pubsub.pubsub.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a message entity.
 * This class provides a structure for encapsulating message details such as the unique identifier,
 * content, sender, and timestamp.
 * <p>
 * It offers getter and setter methods to access and modify the properties, as well as a default constructor
 * and a string representation of the object.
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class MessageDTO {
    private String id;
    private String content;
    private String sender;
    private LocalDateTime timestamp;

    public MessageDTO(String content, String s) {
        this.content = content;
        this.sender = s;

    }
}