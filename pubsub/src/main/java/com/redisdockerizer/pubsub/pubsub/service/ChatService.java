package com.redisdockerizer.pubsub.pubsub.service;

import com.redisdockerizer.pubsub.pubsub.subscriber.LoggingSubscriber;
import com.redisdockerizer.pubsub.pubsub.subscriber.MetricsSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class that manages Redis Pub/Sub channel subscriptions. This class is responsible
 * for subscribing and unsubscribing to Redis channels, maintaining a registry of active
 * subscriptions, and processing incoming messages through designated subscribers for logging
 * and metrics collection.
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ConcurrentHashMap<String, ChannelTopic> activeSubscriptions = new ConcurrentHashMap<>();
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    private final LoggingSubscriber loggingSubscriber;
    private final MetricsSubscriber metricsSubscriber;

    public static final String DEFAULT_CHANNEL_GENERAL = "demo.chat.general";
    public static final String DEFAULT_CHANNEL_RANDOM  = "demo.chat.random";

    /**
     * Constructs a new ChatService instance to manage Redis Pub/Sub channel subscriptions.
     * Initializes default subscriptions to predefined channels and sets up logging and metrics
     * subscribers for message processing.
     *
     * @param redisMessageListenerContainer the container for managing Redis message listeners
     * @param loggingSubscriber the subscriber responsible for logging incoming messages
     * @param metricsSubscriber the subscriber responsible for recording metrics related to message events
     */
    public ChatService(RedisMessageListenerContainer redisMessageListenerContainer,
                       LoggingSubscriber loggingSubscriber,
                       MetricsSubscriber metricsSubscriber) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.loggingSubscriber = loggingSubscriber;
        this.metricsSubscriber = metricsSubscriber;

        // Subscribe defaults at startup
        subscribeToChannel(DEFAULT_CHANNEL_GENERAL);
        subscribeToChannel(DEFAULT_CHANNEL_RANDOM);
    }

    /**
     * Subscribes to a Redis Pub/Sub channel by creating a topic and adding designated
     * message listeners to the channel. If the channel is already subscribed to,
     * logs a warning and takes no further action.
     *
     * @param channelName the name of the channel to subscribe to, with or without the "demo.chat." prefix
     */
    public void subscribeToChannel(String channelName) {
        String topicName = getTopicName(channelName);
        if (!activeSubscriptions.containsKey(topicName)) {
            ChannelTopic topic = new ChannelTopic(topicName);
            redisMessageListenerContainer.addMessageListener(loggingSubscriber, topic);
            redisMessageListenerContainer.addMessageListener(metricsSubscriber, topic);
            activeSubscriptions.put(topicName, topic);
            log.info("Subscribed to channel: {}", topicName);
        } else {
            log.warn("Already subscribed to channel: {}", topicName);
        }
    }

    /**
     * Unsubscribes the given channel by removing all associated subscribers and clearing it
     * from the active subscriptions' registry. This action is idempotent: if the channel is not
     * currently subscribed, a warning is logged, and no further action is taken.
     *
     * @param channelName the name of the channel to unsubscribe, with or without the "demo.chat." prefix
     */
    public void unsubscribeFromChannel(String channelName) {
        String topicName = getTopicName(channelName);
        ChannelTopic topic = activeSubscriptions.remove(topicName);
        if (topic != null) {
            redisMessageListenerContainer.removeMessageListener(loggingSubscriber, topic);
            redisMessageListenerContainer.removeMessageListener(metricsSubscriber, topic);
            log.info("Unsubscribed from channel: {}", topicName);
        } else {
            log.warn("Not subscribed to channel: {}", topicName);
        }
    }

    /**
     * Retrieves the set of currently active Redis Pub/Sub channel subscriptions.
     *
     * @return a set of strings representing the names of active subscriptions
     */
    public Set<String> getActiveSubscriptions() {
        return new HashSet<>(activeSubscriptions.keySet());
    }

    /**
     * Generates a topic name by ensuring it has the "demo.chat." Prefix.
     * If the provided room name already starts with "demo.chat.", it is returned unchanged.
     * Otherwise, the prefix "demo.chat." Is prepended to the given room name.
     *
     * @param roomName the name of the room or channel, with or without the "demo.chat." prefix
     * @return the fully qualified topic name starting with "demo.chat."
     */
    private String getTopicName(String roomName) {
        if (roomName.startsWith("demo.chat.")) {
            return roomName;
        }
        return "demo.chat." + roomName;
    }
}