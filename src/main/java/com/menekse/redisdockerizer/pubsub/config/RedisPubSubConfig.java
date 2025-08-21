package com.menekse.redisdockerizer.pubsub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for setting up Redis Pub/Sub functionality.
 * This class provides the configuration for Redis message listener and template.
 * It ensures proper serialization of Redis messages and sets up the necessary beans
 * for publishing and subscribing to Redis channels.
 */
@Configuration
public class RedisPubSubConfig {

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
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates a RedisMessageListenerContainer bean, which listens for messages
     * from Redis channels. The container is responsible for subscribing to the channels
     * and dispatching the messages to listeners.
     * This container is essential for handling the incoming messages for the Pub/Sub model.
     *
     * @param connectionFactory the RedisConnectionFactory used to establish a connection to Redis
     * @return a RedisMessageListenerContainer instance configured with the connection factory
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
