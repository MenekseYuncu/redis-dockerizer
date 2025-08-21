package com.integration.redisdockerizer.pubsub.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integration.redisdockerizer.pubsub.model.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * RedisPublisher is a service class responsible for interacting with Redis to publish messages
 * to Redis channels. This class serves as the bridge between the application and
 * the Redis Pub/sub * system, enabling asynchronous communication between different parts of the system using Redis.
 * <p>
 * The class provides two main types of message publishing:
 * 1. **Complex message publishing**: This involves publishing messages encapsulated in a `MessageDTO` object,
 * which includes fields like `content` and `sender`. These messages are serialized to JSON before being
 * sent to Redis channels.
 * 2. **Simple message publishing**: This involves sending keymanagement string messages to Redis channels.
 * <p>
 * The `RedisPublisher` class is used to decouple the process of message publication from other business logic,
 * allowing the system to asynchronously send messages to various Redis channels. Redis' Pub/Sub system
 * helps to implement efficient event-driven architectures.
 */
@Service
public class RedisPublisher {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Publishes a complex message (MessageDTO) to a specified Redis channel.
     * <p>
     * This method is used when the application needs to publish a structured message (e.g., with content and sender details)
     * to a Redis channel. The message is serialized into a JSON string before being sent to Redis, ensuring that the message
     * can be easily consumed by subscribers that are listening to the channel.
     * <p>
     * The method generates a unique ID for each message, ensuring that every message can be uniquely identified.
     * <p>
     * Why is this method needed?
     * - It allows the application to send complex messages with structured data, such as content and sender details,
     * to other parts of the system through Redis.
     * - This method enables the application to interact with Redis in a standardized format (JSON), which can be easily
     * consumed by subscribers.
     * - It ensures message integrity by assigning a unique ID to each message.
     *
     * @param channel The Redis channel to which the message will be published.
     * @param message The complex message to be published, encapsulated in a MessageDTO object.
     */
    public void publishMessage(String channel, MessageDTO message) {
        try {
            message.setId(UUID.randomUUID().toString());

            String jsonMessage = objectMapper.writeValueAsString(message);

            redisTemplate.convertAndSend(channel, jsonMessage);

            System.out.println("Message sent - Channel: " + channel + ", Message: " + jsonMessage);
        } catch (JsonProcessingException e) {
            System.err.println("Error occurred while converting the message to JSON: " + e.getMessage());
        }
    }

    /**
     * Publishes a simple text message to a specified Redis channel.
     * <p>
     * This method is used when the application needs to send simple, unstructured text messages (e.g., notifications, alerts)
     * to Redis channels. These messages are sent as plain strings without any additional metadata or structure.
     * <p>
     * Why is this method needed?
     * - It allows the application to send keymanagement text messages quickly to Redis channels.
     * - This method is ideal for scenarios where the message content is simple and does not require complex serialization.
     *
     * @param channel The Redis channel to which the message will be published.
     * @param message The simple text message to be published.
     */
    public void publishSimpleMessage(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);

        System.out.println("Simple message sent - Channel: " + channel + ", Message: " + message);
    }
}