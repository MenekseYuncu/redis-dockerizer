package com.redisdockerizer.pubsub.pubsub.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * Configuration class for setting up Redis Pub/Sub infrastructure in a Spring application.
 * It defines beans for Redis connection management, message serialization, message listeners,
 * and task execution, enabling efficient interaction and communication with a Redis server
 * in a Pub/Sub context.
 */
@Configuration
public class RedisPubSubConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    /**
     * Creates a RedisTemplate bean for interacting with Redis.
     * The RedisTemplate is configured with key and value serializers to handle the data format.
     * The key serializer is set to StringRedisSerializer, and the value serializer is set to
     * GenericJackson2JsonRedisSerializer, allowing the template to work with String keys and
     * JSON-encoded object values.
     *
     * @param connectionFactory the RedisConnectionFactory used to connect to Redis
     * @return a RedisTemplate instance configured for Pub/Sub operations
     */
    @Bean(name = "redisPubSubTemplate")
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer redisJsonSerializer
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use the configured serializer that has JavaTimeModule
        template.setValueSerializer(redisJsonSerializer);
        template.setHashValueSerializer(redisJsonSerializer);

        template.afterPropertiesSet();
        return template;
    }


    /**
     * Creates and configures a {@link LettuceConnectionFactory} bean for establishing
     * connections with a standalone Redis server. The factory is initialized
     * using {@link RedisStandaloneConfiguration}, which is set with the host name
     * and port of the Redis server.
     *
     * @return a configured instance of {@link LettuceConnectionFactory} for managing
     * connections to a standalone Redis server.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(config);
    }


    /**
     * Configures and creates a {@link GenericJackson2JsonRedisSerializer} bean for serializing
     * and deserializing objects to and from JSON using Jackson. The method sets up an
     * {@link ObjectMapper} with additional configuration such as registering the {@link JavaTimeModule}
     * to handle Java 8 date/time types, disabling specific serialization and deserialization features,
     * and customizing property visibility.
     *
     * @param springObjectMapper the {@link ObjectMapper} instance provided by Spring, used as the base
     *                           configuration for creating the customized serializer
     * @return a configured instance of {@link GenericJackson2JsonRedisSerializer} for handling
     *         JSON serialization and deserialization in Redis interactions
     */
    @Bean
    public GenericJackson2JsonRedisSerializer redisJsonSerializer(ObjectMapper springObjectMapper) {
        ObjectMapper om = springObjectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        om.configure(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES, false);

        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        return new GenericJackson2JsonRedisSerializer(om);
    }

    /**
     * Configures and creates a {@link Jackson2JsonRedisSerializer} bean for serializing
     * and deserializing objects to and from JSON using Jackson. The method also registers
     * the {@link JavaTimeModule}, enabling proper handling of Java 8 date and time types
     * during serialization and deserialization.
     *
     * @return a configured instance of {@link Jackson2JsonRedisSerializer} for general
     * purpose JSON serialization and deserialization in Redis interactions.
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }

    /**
     * Configures and initializes a RedisMessageListenerContainer bean for handling
     * Redis Pub/Sub message listening. The container is set with a connection
     * factory to establish communication with the Redis server and a custom
     * task executor for managing listener threads, ensuring efficient concurrent
     * processing of messages.
     *
     * @return an instance of {@link RedisMessageListenerContainer} configured with
     * a connection factory and a task executor for handling Redis Pub/Sub
     * message listening.
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory());
        container.setTaskExecutor(messageListenerTaskExecutor());
        return container;
    }

    /**
     * Creates and configures a {@link ThreadPoolTaskExecutor} bean specifically designed
     * for handling message listener tasks. The executor is configured with a fixed pool size,
     * a queue capacity for managing task submissions, and a thread name prefix for better
     * debugging and log readability. It ensures efficient handling of concurrent tasks
     * for message-listening operations in a Redis Pub/Sub context.
     *
     * @return an instance of {@link ThreadPoolTaskExecutor} initialized with specific
     * core pool size, maximum pool size, queue capacity, and thread name prefix
     * for managing message listener tasks.
     */
    @Bean
    public ThreadPoolTaskExecutor messageListenerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("RedisMessageListener-");
        executor.initialize();
        return executor;
    }
}
