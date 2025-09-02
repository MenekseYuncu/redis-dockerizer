package com.redisdockerizer.pubsub.pubsub.subscriber;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redisdockerizer.pubsub.pubsub.model.Message;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The MetricsSubscriber class serves as a listener for messages on a Redis pub/sub channel.
 * Its primary function is to process incoming messages, track message counts across different
 * chat rooms, and periodically log summary information for monitoring purposes.
 * <p>
 * This class implements the {@link MessageListener} interface provided by Spring Data Redis,
 * enabling it to consume messages matching specific channel patterns or subscriptions.
 * It performs several key tasks:
 * - Parses and deserializes message payloads into {@link Message} objects using a customized {@link ObjectMapper}.
 * - Updates message traffic statistics for each chat room in a thread-safe manner using a {@link ConcurrentHashMap}.
 * - Logs summary messages at configurable intervals via SLF4J logging.
 * <p>
 * Key features:
 * - Thread-safe handling of message counts through {@link AtomicLong}.
 * - Real-time updates and retrieval of the last processed summary message.
 * - Resilient error handling during message deserialization and processing.
 * <p>
 * This component is designed with scalability and concurrency in mind, making it suitable
 * for high-throughput messaging systems.
 */
@Component
public class MetricsSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MetricsSubscriber.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final int LOG_INTERVAL = 10;

    private final ObjectMapper mapper;

    /**
     * Tracks the count of messages processed per chat room.
     * <p>
     * This map holds the message counts for each chat room, where the key is the
     * name of the chat room (as a String), and the value is an AtomicLong
     * representing the count of messages processed for that specific room. This
     * variable is updated as messages are received and processed by the
     * {@code MetricsSubscriber}.
     */
    @Getter
    private final ConcurrentHashMap<String, AtomicLong> messageCounts = new ConcurrentHashMap<>();

    /**
     * Tracks the last summary message generated during the processing of chat messages.
     * <p>
     * The variable is updated periodically, typically when a predefined number
     * of messages have been processed for a specific chat room, providing
     * a human-readable summary of the most recent activity.
     * The default value is "No messages processed yet." And it is updated
     * when a significant event, such as a message count reaching a logging milestone, has occurred.
     * <p>
     * Thread safety is ensured by marking this variable as `volatile`, allowing
     * visibility across threads without requiring external synchronization.
     */
    @Getter
    private volatile String lastSummary = "No messages processed yet.";


    /**
     * Constructor for the MetricsSubscriber class that initializes a customized ObjectMapper
     * for handling serialization and deserialization of Java objects, particularly with Java Time Module
     * and specific serialization features.
     *
     * @param springObjectMapper the ObjectMapper instance provided by Spring, which will be cloned
     *                           and configured with additional settings to customize serialization behavior.
     */
    public MetricsSubscriber(ObjectMapper springObjectMapper) {
        this.mapper = springObjectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    /**
     * Handles incoming messages from a Redis subscription and processes them to track
     * message counts per chat room while logging progress at defined intervals.
     *
     * @param message the incoming Redis message to be processed, containing the message payload
     *                and metadata such as the channel.
     * @param pattern the pattern (if any) that matched this subscription can be null if not applicable.
     */
    @Override
    public void onMessage(org.springframework.data.redis.connection.Message message, byte[] pattern) {
        final String channel = new String(message.getChannel(), CHARSET);
        try {
            final Message chatMessage = mapper.readValue(message.getBody(), Message.class);
            final String room = chatMessage.getRoom();

            if (room == null || room.isBlank()) {
                log.warn("Skipping message without room. channel={}, payload={}",
                        channel, new String(message.getBody(), CHARSET));
                return;
            }

            final long count = messageCounts
                    .computeIfAbsent(room, k -> new AtomicLong())
                    .incrementAndGet();

            if (count % LOG_INTERVAL == 0) {
                lastSummary = String.format("Room=%s -> %d messages processed.", room, count);
                log.info(lastSummary);
            }
        } catch (Exception e) {
            log.error("Failed to deserialize message on channel {}: {}",
                    channel, new String(message.getBody(), CHARSET), e);
        }
    }
}