package com.menekse.redisdockerizer.pubsub.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menekse.redisdockerizer.pubsub.model.MessageDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;


/**
 * RedisSubscriber is a service class responsible for subscribing to Redis channels
 * and listening for incoming messages. This class listens for messages on specific Redis channels
 * and processes the messages accordingly. It supports both complex messages (e.g., serialized `MessageDTO`)
 * and simple text messages.
 * <p>
 * The subscriber class is integral to the Pub/Sub pattern where it subscribes to specific channels
 * and reacts to events/messages that are published to those channels.
 * <p>
 * The class supports the following key actions:
 * - **Subscribing to channels**: Adding listeners to Redis channels so that it can process messages as they arrive.
 * - **Unsubscribing from channels**: Removing listeners from channels when they are no longer needed.
 * - **Processing messages**: Handling both structured messages (deserialized `MessageDTO`) and simple text messages.
 */
@Service
public class RedisSubscriber implements MessageListener {

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Initializes the RedisSubscriber by subscribing to predefined channels.
     * This method is called after the bean is constructed (using the @PostConstruct annotation).
     * <p>
     * Why is this method needed?
     * - It ensures that the subscriber starts listening to channels as soon as the service is initialized.
     * - This is a useful setup step to automatically subscribe to channels such as "test-channel", "notifications",
     * and "user-messages" when the application starts.
     */
    @PostConstruct
    public void init() {
        subscribeToChannel("test-channel");
        subscribeToChannel("notifications");
        subscribeToChannel("user-messages");
    }

    /**
     * Subscribes to a given Redis channel.
     * <p>
     * This method adds the current subscriber as a listener to a Redis channel. Once subscribed,
     * any messages published to that channel will trigger the `onMessage` method to process the message.
     * <p>
     * Why is this method needed?
     * - It allows the system to listen for messages on specified Redis channels, which is essential in a Pub/Sub model.
     * - The method provides flexibility to subscribe to any Redis channel by passing the channel name dynamically.
     *
     * @param channelName The name of the Redis channel to subscribe to.
     */
    public void subscribeToChannel(String channelName) {
        redisMessageListenerContainer.addMessageListener(this, new ChannelTopic(channelName));
        System.out.println("Listening to channel: " + channelName);
    }

    /**
     * Unsubscribes from a given Redis channel.
     * <p>
     * This method removes the current subscriber as a listener from the specified Redis channel.
     * It ensures that the subscriber no longer receives messages from that channel.
     * <p>
     * Why is this method needed?
     * - It allows the system to stop listening to specific Redis channels, providing flexibility to unsubscribe
     * when certain functionality is no longer needed.
     * - Helps in resource management by unsubscribing from unused channels.
     *
     * @param channelName The name of the Redis channel to unsubscribe from.
     */
    public void unsubscribeFromChannel(String channelName) {
        redisMessageListenerContainer.removeMessageListener(this, new ChannelTopic(channelName));
        System.out.println("Stopped listening to channel: " + channelName);
    }

    /**
     * This method is called when a message is received from a Redis channel the subscriber is listening to.
     * It processes both structured (JSON) and unstructured (string) messages.
     * <p>
     * Why is this method needed?
     * - It is the central method that processes messages published to Redis channels.
     * - It ensures that incoming messages are handled appropriately, either as structured `MessageDTO` objects or as simple strings.
     *
     * @param message The Redis message that was published to the subscribed channel.
     * @param pattern The pattern used for message filtering (not used in this implementation).
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String messageBody = new String(message.getBody());

        System.out.println("\n=== NEW MESSAGE RECEIVED ===");
        System.out.println("Channel: " + channel);
        System.out.println("Raw Message: " + messageBody);

        try {
            MessageDTO parsedMessage = objectMapper.readValue(messageBody, MessageDTO.class);
            System.out.println("Parsed Message: " + parsedMessage);

            processMessage(channel, parsedMessage);

        } catch (JsonProcessingException e) {
            System.out.println("Simple String Message: " + messageBody);
            processSimpleMessage(channel, messageBody);
        }

        System.out.println("=========================\n");
    }


    /**
     * Processes a structured message (MessageDTO) based on the channel it was received from.
     * <p>
     * Why is this method needed?
     * - It allows the system to differentiate between message types and handle them appropriately.
     * - The method processes messages based on their channel, allowing for specific logic to be applied
     * based on the channel's use case (e.g., notifications, user messages, etc.).
     *
     * @param channel The Redis channel from which the message was received.
     * @param message The MessageDTO that was deserialized from the JSON message.
     */
    private void processMessage(String channel, MessageDTO message) {
        switch (channel) {
            case "notifications":
                System.out.println("Processing Notification: " + message.getContent());
                break;
            case "user-messages":
                System.out.println("Processing User Message from " + message.getSender() + ": " + message.getContent());
                break;
            default:
                System.out.println("Processing General Message: " + message.getContent());
        }
    }

    /**
     * Processes a simple string message received from a Redis channel.
     * <p>
     * Why is this method needed?
     * - It provides a mechanism to handle messages that are not in structured JSON format.
     * - This method ensures that the application can handle simple, unstructured messages in addition to complex ones.
     *
     * @param channel The Redis channel from which the message was received.
     * @param message The simple string message received from the channel.
     */
    private void processSimpleMessage(String channel, String message) {
        System.out.println("Processing Simple Message - Channel: " + channel + ", Message: " + message);
    }
}