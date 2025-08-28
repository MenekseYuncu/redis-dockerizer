package com.integration.redisdockerizer.pubsub.controller;

import com.integration.redisdockerizer.pubsub.model.MessageDTO;
import com.integration.redisdockerizer.pubsub.publisher.RedisPublisher;
import com.integration.redisdockerizer.pubsub.subscriber.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PubSubController handles the Redis Publish/Subscribe operations.
 * Provides endpoints for publishing messages, subscribing to channels,
 * and retrieving the status of the Redis Pub/Sub service.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pubsub")
public class PubSubController {

    private final RedisPublisher redisPublisher;

    private final RedisSubscriber redisSubscriber;

    /**
     * Publishes a message to a specified Redis channel.
     *
     * @param request The request body containing:
     *                - channel: The Redis channel to publish the message to (String).
     *                - content: The content of the message (String).
     *                - Sender: (Optional) The sender of the message (String). The default is "Anonymous".
     * @return A ResponseEntity containing the status and message of the operation:
     * - status: "success" on successful message publication.
     * - message: "Message successfully sent" on success.
     * - Channel: The Redis channel to which the message was published.
     * <p>
     * Expected Request Body:
     * {
     * "channel": "string", // The Redis channel to publish the message to.
     * "content": "string", // The content of the message.
     * "sender": "string" // (Optional) The sender of the message. The default is "Anonymous".
     * }
     * <p>
     * Expected Response Body (on success):
     * {
     * "status": "success", // The status of the message publication.
     * "message": "Message successfully sent.",
     * "channel": "string" // The Redis channel to which the message was published.
     * }
     * <p>
     * Expected Response Body (on failure):
     * {
     * "error": "Channel and content fields are required."
     * }
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publishMessage(@RequestBody Map<String, String> request) {
        String channel = request.get("channel");
        String content = request.get("content");
        String sender = request.get("sender");

        // Input validation
        if (!StringUtils.hasText(channel) || !StringUtils.hasText(content)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Channel and content fields are required.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Length validation
        if (channel.length() > 100 || content.length() > 10000) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Channel or content too long.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Channel name validation (alphanumeric and hyphens only)
        if (!channel.matches("^[a-zA-Z0-9\\-]+$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid channel name. Use only letters, numbers, and hyphens.");
            return ResponseEntity.badRequest().body(error);
        }

        MessageDTO message = new MessageDTO(content, sender != null ? sender : "Anonymous");
        redisPublisher.publishMessage(channel, message);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Message successfully sent.");
        response.put("channel", channel);

        return ResponseEntity.ok(response);
    }

    /**
     * Publishes a simple message to a specified Redis channel.
     *
     * @param request The request body containing:
     *                - channel: The Redis channel to publish the message to (String).
     *                - message: The content of the simple message (String).
     * @return A ResponseEntity containing the status and message of the operation:
     * - status: "success" on successful message publication.
     * - message: "Simple message successfully sent" on success.
     * - Channel: The Redis channel to which the message was published.
     * <p>
     * Expected Request Body:
     * {
     * "channel": "string", // The Redis channel to publish the message to.
     * "message": "string" // The content of the simple message.
     * }
     * <p>
     * Expected Response Body (on success):
     * {
     * "status": "success", // The status of the message publication.
     * "message": "Simple message successfully sent.",
     * "channel": "string" // The Redis channel to which the message was published.
     * }
     * <p>
     * Expected Response Body (on failure):
     * {
     * "error": "Channel and message fields are required."
     * }
     */
    @PostMapping("/publish/simple")
    public ResponseEntity<Map<String, String>> publishSimpleMessage(@RequestBody Map<String, String> request) {
        String channel = request.get("channel");
        String message = request.get("message");

        // Input validation
        if (!StringUtils.hasText(channel) || !StringUtils.hasText(message)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Channel and message fields are required.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Length validation
        if (channel.length() > 100 || message.length() > 10000) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Channel or message too long.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Channel name validation
        if (!channel.matches("^[a-zA-Z0-9\\-]+$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid channel name. Use only letters, numbers, and hyphens.");
            return ResponseEntity.badRequest().body(error);
        }

        redisPublisher.publishSimpleMessage(channel, message);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Simple message successfully sent.");
        response.put("channel", channel);

        return ResponseEntity.ok(response);
    }

    /**
     * Subscribes to a specified Redis channel.
     *
     * @param request The request body containing:
     *                - channel: The Redis channel to subscribe to (String).
     * @return A ResponseEntity containing the status and message of the subscription:
     * - status: "success" on a successful subscription.
     * - message: "Successfully subscribed to the channel."
     * - Channel: The Redis channel to which the subscription was made.
     * <p>
     * Expected Request Body:
     * {
     * "channel": "string"  // The Redis channel to subscribe to.
     * }
     * <p>
     * Expected Response Body (on success):
     * {
     * "status": "success",  // The status of the subscription.
     * "message": "Successfully subscribed to the channel.",
     * "channel": "string"   // The Redis channel to which the subscription was made.
     * }
     * <p>
     * Expected Response Body (on failure):
     * {
     * "error": "Channel field is required."
     * }
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribeToChannel(@RequestBody Map<String, String> request) {
        String channel = request.get("channel");

        // Input validation
        if (!StringUtils.hasText(channel)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Channel field is required.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Channel name validation
        if (!channel.matches("^[a-zA-Z0-9\\-]+$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid channel name. Use only letters, numbers, and hyphens.");
            return ResponseEntity.badRequest().body(error);
        }

        redisSubscriber.subscribeToChannel(channel);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Successfully subscribed to the channel.");
        response.put("channel", channel);

        return ResponseEntity.ok(response);
    }

    /**
     * Unsubscribes from a specified Redis channel.
     *
     * @param request The request body containing:
     *                - channel: The Redis channel to unsubscribe from (String).
     * @return A ResponseEntity containing the status and message of the unsubscription:
     * - status: "success" on successful unsubscription.
     * - message: "Unsubscribed from the channel."
     * - Channel: The Redis channel from which the unsubscription was made.
     * <p>
     * Expected Request Body:
     * {
     * "channel": "string" // The Redis channel to unsubscribe from.
     * }
     * <p>
     * Expected Response Body (on success):
     * {
     * "status": "success", // The status of the unsubscription.
     * "message": "Unsubscribed from the channel.",
     * "channel": "string" // The Redis channel from which the unsubscription was made.
     * }
     * <p>
     * Expected Response Body (on failure):
     * {
     * "error": "Channel field is required."
     * }
     */
    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribeFromChannel(@RequestBody Map<String, String> request) {
        String channel = request.get("channel");

        // Input validation
        if (!StringUtils.hasText(channel)) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Channel field is required.");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Channel name validation
        if (!channel.matches("^[a-zA-Z0-9\\-]+$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid channel name. Use only letters, numbers, and hyphens.");
            return ResponseEntity.badRequest().body(error);
        }

        redisSubscriber.unsubscribeFromChannel(channel);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Unsubscribed from the channel.");
        response.put("channel", channel);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns the current status of the Redis Pub/Sub service.
     *
     * @return A ResponseEntity containing the status and a message indicating that the Redis service is active.
     * <p>
     * Expected Response Body:
     * {
     * "status": "active", // Indicates that the Redis service is running.
     * "Message": "Redis Pub/Sub service is running"
     * }
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "active");
        status.put("message", "Redis Pub/Sub service is running");

        return ResponseEntity.ok(status);
    }
}
