package com.redisdockerizer.pubsub.pubsub.publisher;

import com.redisdockerizer.pubsub.pubsub.model.Message;
import com.redisdockerizer.pubsub.pubsub.model.PublishMessageRequest;
import com.redisdockerizer.pubsub.pubsub.util.MessageLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * This service class is responsible for handling the publication of messages
 * to Redis channels. It provides methods for publishing messages directly,
 * or via structured*/
@Service
@RequiredArgsConstructor
public class RedisPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisPublisher.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageLoader messageLoader;


    /**
     * Publishes a message to a specified channel. The method normalizes the channel
     * and message before publishing, ensuring that all required fields are properly set.
     * The message is then sent to the Redis server, using the provided channel as the topic.
     * Logging is included for debugging and error tracking.
     *
     * @param channel the name of the channel to publish the message to; must not be null or blank
     * @param message the Message object containing the data to be published; must not be null
     */
    public void publishMessage(String channel, Message message) {
        try {
            String topic = normalizeChannel(channel);
            normalizeMessageForPublish(topic, message);
            redisTemplate.convertAndSend(topic, message);
            log.debug("Published message to channel {}: {}", topic, message.getId());
        } catch (Exception e) {
            log.error("Error publishing message to channel {}: {}", channel, message, e);
        }
    }

    /**
     * Publishes a message to the specified channel based on the provided request.
     * Constructs a {@link Message} object using the given channel and {@link PublishMessageRequest},
     * and sends it to the channel.
     *
     * @param channel the name of the channel to which the message will be published; must not be null or blank
     * @param request the {@link PublishMessageRequest} containing data such as the user and the message content; must not be null
     */
    public void publishRequest(String channel, PublishMessageRequest request) {
        Message message = buildMessageFromRequest(channel, request);
        publishMessage(channel, message);
    }

    /**
     * Retrieves the Redis template to be used for performing health checks.
     *
     * @return an instance of {@code RedisTemplate<String, Object>} primarily used for health checks
     */
    public RedisTemplate<String, Object> getRedisTemplateForHealthCheck() {
        return redisTemplate;
    }

    /**
     * Publishes predefined startup messages to the "demo.chat.general" channel after the
     * bean initialization. This method is annotated with {@code @PostConstruct}, ensuring
     * it runs automatically during the startup phase.
     * <p>
     * The messages are loaded using the {@code messageLoader} and subsequently published
     * to the specified channel using the {@code publishMessage} method. Logging is used
     * to track the start and completion of the process as well as the number of messages published.
     * <p>
     * This method is primarily used to initialize the system with default or required
     * messages upon application startup.
     */
    @PostConstruct
    public void publishStartupMessages() {
        log.info("Publishing startup messages to demo.chat.general...");
        List<Message> messages = messageLoader.getMessages();
        messages.forEach(msg -> publishMessage("demo.chat.general", msg));
        log.info("Finished publishing {} startup messages.", messages.size());
    }

    /**
     * Constructs a {@link Message} object based on the provided channel and
     * {@link PublishMessageRequest}. The method extracts the room information
     * from the channel, assigns a unique ID, and populates the message with
     * the data from the request along with default values for type, status,
     * timestamp, and last modified time.
     *
     * @param channel the channel name, used to extract the room information
     * @param request the {@link PublishMessageRequest} containing user information and message content
     * @return a fully constructed {@link Message} object ready for publishing
     */
    private Message buildMessageFromRequest(String channel, PublishMessageRequest request) {
        String room = extractRoomFromChannel(normalizeChannel(channel));
        return Message.builder()
                .id("msg-" + UUID.randomUUID().toString().substring(0, 8))
                .user(request.getUser())
                .room(room)
                .text(request.getText())
                .type("text")
                .timestamp(Instant.now())
                .status("delivered")
                .lastModified(Instant.now())
                .build();
    }

    /**
     * Normalizes the given message before publishing by ensuring all required fields are properly set.
     * If any required field is null or missing, default values are assigned.
     * The method is designed to prevent incomplete or invalid messages from being published.
     *
     * @param channel the name of the channel to which the message is being published;
     *                used to extract the room name if it is missing in the message
     * @param message the Message object to normalize; must not be null
     * @throws NullPointerException if the message is null
     */
    private void normalizeMessageForPublish(String channel, Message message) {
        Objects.requireNonNull(message, "message must not be null");
        if (message.getId() == null) {
            message.setId("msg-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (message.getRoom() == null || message.getRoom().isBlank()) {
            message.setRoom(extractRoomFromChannel(channel));
        }
        if (message.getType() == null) {
            message.setType("text");
        }
        if (message.getStatus() == null) {
            message.setStatus("delivered");
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(Instant.now());
        }
        message.setLastModified(Instant.now());
    }

    /**
     * Normalizes the given channel string by trimming any leading and trailing whitespace.
     * Throws an {@link IllegalArgumentException} if the channel is null or blank.
     *
     * @param channel the channel string to normalize; must not be null or blank
     * @return the trimmed channel string
     * @throws IllegalArgumentException if the channel is null or blank
     */
    private String normalizeChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("channel must not be null/blank");
        }
        return channel.trim();
    }

    /**
     * Extracts the room name from a given channel string. The room name is
     * identified as the substring following the last dot ('.') in the channel.
     * If the channel does not contain a dot, or if the dot is the last character
     * in the channel, the entire channel string is returned.
     *
     * @param channel the full channel name from which the room name is to be extracted
     * @return the extracted room name or the original channel name if no valid room name is found
     */
    private String extractRoomFromChannel(String channel) {
        int lastDot = channel.lastIndexOf('.');
        return (lastDot >= 0 && lastDot < channel.length() - 1)
                ? channel.substring(lastDot + 1)
                : channel;
    }
}
