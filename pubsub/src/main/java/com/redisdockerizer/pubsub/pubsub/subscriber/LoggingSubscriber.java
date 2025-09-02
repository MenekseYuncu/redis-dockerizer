package com.redisdockerizer.pubsub.pubsub.subscriber;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redisdockerizer.pubsub.pubsub.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The LoggingSubscriber class is a component that listens to messages
 * from a Redis Pub/Sub channel. This class deserializes received messages
 * into the {@link Message} type and logs message details to the application logs.
 * <p>
 * The logging functionality includes:
 * - Logging the room, user, and text from the deserialized message.
 * - Logging a warning if the message is null or unparseable.
 * - Logging an error if deserialization fails.
 * <p>
 * This class uses a custom-configured {@link Jackson2JsonRedisSerializer} for deserialization
 * of messages, which is initialized with an {@link ObjectMapper} tailored for handling
 * date and time data.
 * <p>
 * Implements the {@link MessageListener}
 * interface for subscribing to and handling Redis messages.
 */
@Component
public class LoggingSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingSubscriber.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final RedisSerializer<Message> serializer;

    /**
     * Constructs a new instance of the LoggingSubscriber.
     * <p>
     * Configures the provided {@code ObjectMapper} by creating a copy, registering the {@code JavaTimeModule},
     * and customizing serialization and deserialization behavior specific to date and time handling.
     * The configured {@code ObjectMapper} is then used to initialize a {@link Jackson2JsonRedisSerializer}
     * for deserializing messages of type {@link Message}.
     *
     * @param springObjectMapper the Spring-configured {@link ObjectMapper} to use as a base for creating
     *                           the serializer.
     */
    public LoggingSubscriber(ObjectMapper springObjectMapper) {
        ObjectMapper mapper = springObjectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        this.serializer = new Jackson2JsonRedisSerializer<>(mapper, Message.class);
    }

    /**
     * Handles incoming messages from a Redis Pub/Sub channel.
     * Deserializes the message payload and logs the message details if successfully parsed.
     * In case of a null or unparsable message, logs a warning. Logs an error if deserialization fails.
     *
     * @param message the message received from the Redis channel, containing the channel and payload.
     * @param pattern the pattern that matched the message channel, provided by the Redis listener.
     */
    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), CHARSET);
        try {
            Message chatMessage = serializer.deserialize(message.getBody());
            if (chatMessage != null) {
                log.info("[{}] {}: {}", chatMessage.getRoom(), chatMessage.getUser(), chatMessage.getText());
            } else {
                log.warn("Received null or unparseable message on channel {}", channel);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize message on channel {}: {}",
                    channel, new String(message.getBody(), CHARSET), e);
        }
    }
}