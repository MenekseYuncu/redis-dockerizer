package com.redisdockerizer.pubsub.pubsub.controller;

import com.redisdockerizer.pubsub.pubsub.model.PublishMessageRequest;
import com.redisdockerizer.pubsub.pubsub.publisher.RedisPublisher;
import com.redisdockerizer.pubsub.pubsub.service.ChatService;
import com.redisdockerizer.pubsub.pubsub.subscriber.MetricsSubscriber;
import com.redisdockerizer.pubsub.pubsub.util.MessageLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The ChatController class provides RESTful APIs for managing chat functionalities
 * such as publishing messages, subscribing to channels, retrieving metrics, and
 * performing health checks. This controller interacts with Redis for message handling
 * and channel subscriptions and exposes endpoints for monitoring and management.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pubsub")
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatService chatService;
    private final MessageLoader messageLoader;
    private final MetricsSubscriber metricsSubscriber;


    /**
     * Publishes a message to a specified Redis channel.
     * <pre>
     * Request:
     * POST /api/pubsub/messages
     * Content-Type: application/json
     * {
     *   "channel": "demo.chat.general",
     *   "user": "alice",
     *   "text": "Hello everyone!"
     * }
     * Response:
     * "Message published to demo.chat.general"
     * </pre>
     *
     * @param request a {@link PublishMessageRequest} object containing the channel name,
     *                user information, and message text to be published
     * @return a string confirming the message was published, including the target channel name
     */
    @PostMapping("/messages")
    public String publishMessage(@RequestBody PublishMessageRequest request) {
        redisPublisher.publishRequest(request.channel(), request);
        return "Message published to " + request.channel();
    }


    /**
     * Publishes a batch of messages to their respective channels.
     * <pre>
     * Request:
     * POST /api/pubsub/messages/batch
     * Content-Type: application/json
     * [
     *   {
     *     "channel": "demo.chat.general",
     *     "user": "alice",
     *     "text": "Hello again everyone!"
     *   },
     *   {
     *     "channel": "demo.chat.devops",
     *     "user": "charlie",
     *     "text": "Is anyone here in the devops room?"
     *   }
     * ]
     * Response:
     * "2 messages published."
     * </pre>
     *
     * @param requests the list of {@link PublishMessageRequest} objects containing
     *                 the message details and the target channels
     * @return a string indicating the number of messages successfully published
     */
    @PostMapping("/messages/batch")
    public String publishBatchMessages(@RequestBody List<PublishMessageRequest> requests) {
        requests.forEach(req -> redisPublisher.publishRequest(req.channel(), req));
        return requests.size() + " messages published.";
    }

    /**
     * Replays startup messages by publishing them to a predefined Redis channel.
     *
     * <pre>
     * Request:
     * POST /api/pubsub/messages/replay
     *
     * Response:
     * "100 startup messages replayed to demo.chat.general."
     * </pre>
     *
     * @return A string confirmation indicating that 100 startup messages were replayed
     *         to the "demo.chat.general" channel.
     */
    @PostMapping("/messages/replay")
    public String replayStartupMessages() {
        messageLoader.getMessages().forEach(
                message -> redisPublisher.publishMessage("demo.chat.general", message)
        );
        return "100 startup messages replayed to demo.chat.general.";
    }


    /**
     * Subscribes to a specified chat channel.
     * <pre>
     * Request:
     * POST /api/pubsub/channels/general/subscribe
     *
     * Response:
     * "Subscribed to channel: general"
     * </pre>
     *
     * @param room the name of the chat room to subscribe to
     * @return a confirmation message indicating the subscription status
     */
    @PostMapping("/channels/{room}/subscribe")
    public String subscribeToChannel(@PathVariable String room) {
        chatService.subscribeToChannel("demo.chat." + room);
        return "Subscribed to channel: " + room;
    }

    /**
     * Unsubscribes from a specified chat channel.
     * <pre>
     * Request:
     * POST /api/pubsub/channels/general/unsubscribe
     *
     * Response:
     * "Unsubscribed from channel: general"
     * </pre>
     *
     * @param room the name of the chat room to unsubscribe from
     * @return a confirmation message indicating the unsubscription status
     */
    @PostMapping("/channels/{room}/unsubscribe")
    public String unsubscribeFromChannel(@PathVariable String room) {
        chatService.unsubscribeFromChannel("demo.chat." + room);
        return "Unsubscribed from channel: " + room;
    }

    /**
     * Retrieves the set of active channel subscriptions.
     * <pre>
     * Request:
     * GET /api/pubsub/channels/subscriptions
     *
     * Response:
     * [
     *   "demo.chat.general",
     *   "demo.chat.devops"
     * ]
     * </pre>
     * @return A set of strings representing the names of currently active channel subscriptions.
     */
    @GetMapping("/channels/subscriptions")
    public Set<String> getActiveSubscriptions() {
        return chatService.getActiveSubscriptions();
    }


    /**
     * Retrieves metrics related to chat message counts and the latest summary.
     * <pre>
     * Request:
     * GET /api/pubsub/metrics
     *
     * Response:
     * {
     *   "totalMessageCountsByRoom": {
     *     "general": 12,
     *     "devops": 5
     *   },
     *   "lastSummary": "Room=general -> 10 messages processed."
     * }
     * </pre>
     *
     * @return A map containing the following keys:
     *         - "totalMessageCountsByRoom": A map of room names to their respective message counts.
     *         - "lastSummary": A string representation of the latest summary.
     */
    @GetMapping("/metrics")
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalMessageCountsByRoom",
                metricsSubscriber.getMessageCounts().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get())));
        metrics.put("lastSummary", metricsSubscriber.getLastSummary());
        return metrics;
    }

    /**
     * Performs a health check for the application and Redis by attempting to
     * establish a connection and execute a `PING` command to verify their status.
     * <pre>
     * Request:
     * GET /api/pubsub/health
     *
     * Response (healthy):
     * "Application and Redis are healthy."
     *
     * Response (failure):
     * "Application or Redis health check failed: Connection refused"
     * </pre>
     *
     * @return A string indicating the health status of the application and Redis.
     */
    @GetMapping("/health")
    public String getHealth() {
        try {
            var factory = redisPublisher.getRedisTemplateForHealthCheck().getConnectionFactory();
            if (factory == null) {
                return "Redis connection factory is not available.";
            }
            factory.getConnection().ping();
            return "Application and Redis are healthy.";
        } catch (Exception e) {
            return "Application or Redis health check failed: " + e.getMessage();
        }
    }
}