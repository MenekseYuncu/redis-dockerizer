package com.redisdockerizer.pubsub.pubsub.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redisdockerizer.pubsub.pubsub.model.Message;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * The {@code MessageLoader} class is responsible for loading message data from a JSON file located on the classpath.
 * It reads and deserializes the data into a list of {@code Message} objects during the initialization phase of the application.
 * <p>
 * This class is annotated with {@code @Component}, making it a Spring-managed bean.
 * The {@code @PostConstruct} method ensures that the message data is loaded automatically after the bean initialization.
 * <p>
 * Key responsibilities:
 * - Loads message definitions from a {@code messages.json} file.
 * - Uses Jackson's {@code ObjectMapper} for JSON deserialization.
 * - Handles any errors during the loading process gracefully by logging errors and initializing an empty message list.
 * <p>
 * Logging behavior:
 * - Logs a success message with the count of messages loaded upon successful initialization.
 * - Logs detailed error information if the initialization encounters a problem (e.g., file not found or JSON parsing issues).
 * <p>
 * Dependencies:
 * - Jackson {@code ObjectMapper} for JSON parsing.
 * - Spring's {@code ClassPathResource} for retrieving the JSON file from the classpath.
 *
 * Thread Safety:
 * - The loaded messages list is immutable after initialization since this class does not provide any methods to modify it.
 *
 * Usage:
 * This class is auto-wired into other Spring components or services that require access to the loaded messages.
 */
@Component
public class MessageLoader {

    private static final Logger log = LoggerFactory.getLogger(MessageLoader.class);
    private final ObjectMapper objectMapper;

    /**
     * Represents a collection of `Message` objects loaded and managed by the `MessageLoader` class.
     * This field stores the list of messages initialized and populated during the lifecycle of the bean.
     * <p>
     * Key characteristics:
     * - Lazily populated through the `init()` method after bean properties are set.
     * - Loaded from an external `messages.json` file located on the classpath.
     * - Holds deserialized `Message` entities if the file is successfully parsed.
     * - Defaults to an empty list in case of an error during file access or parsing.
     * <p>
     * Usage:
     * - Provides access to the complete list of message entries available for the application.
     * - Supports typical list operations for retrieval and processing of messages.
     */
    @Getter
    private List<Message> messages;


    public MessageLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Initializes and loads a list of messages from a `messages.json` file located on the classpath.
     * This method is executed after the bean's properties have been set, due to the `@PostConstruct` annotation.
     * <p>
     * If the file is successfully read, the messages are deserialized into a list of `Message` objects and stored.
     * In the case of an error during file reading or deserialization (e.g., file not found or JSON parsing issues),
     * an empty list of messages is loaded, and an error is logged.
     * <p>
     * Logs:
     * - Info: Logs the number of messages successfully loaded.
     * - Error: Logs any failure to load the messages with the exception details.
     */
    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data/messages.json");
            try (InputStream is = resource.getInputStream()) {
                messages = Arrays.asList(objectMapper.readValue(is, Message[].class));
                log.info("Loaded {} messages from messages.json", messages.size());
            }
        } catch (IOException e) {
            log.error("Failed to load messages from messages.json", e);
            messages = List.of();
        }
    }
}