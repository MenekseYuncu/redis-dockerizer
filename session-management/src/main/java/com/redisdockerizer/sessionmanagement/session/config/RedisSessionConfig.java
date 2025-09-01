package com.redisdockerizer.sessionmanagement.session.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * The RedisSessionConfig class is a Spring Configuration class that defines beans for managing
 * Redis-based HTTP session data. It sets up customized Redis templates for interacting with
 * Redis to store session-related keys and values. The configuration relies on Spring's
 * {@link RedisConnectionFactory} to establish a connection to the Redis server.
 * <p>
 * The beans in this configuration ensure that session data is serialized and deserialized
 * appropriately for proper interaction with the Redis datastore.
 */
@Configuration
public class RedisSessionConfig {


    /**
     * Configures a {@link RedisTemplate} bean that is used by Spring Session to interact with Redis.
     * This template is specifically set up for storing session data (keys and values) as strings.
     * <p>
     * This method creates a RedisTemplate with a connection to Redis and serializes both keys and values as strings
     * using the {@link StringRedisSerializer}. It is used to interact with the Redis server to store, retrieve,
     * and manage HTTP session data.
     * </p>
     *
     * <p>
     * The {@link RedisConnectionFactory} is responsible for establishing the connection to the Redis server,
     * and the {@link StringRedisSerializer} ensures that the keys and values are serialized into strings,
     * which is suitable for HTTP session data.
     * </p>
     *
     * @param connectionFactory the Redis connection factory that is used to establish the Redis connection.
     * @return a configured RedisTemplate for session management.
     */
    @Bean(name = "redisSessionTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Configures and returns a {@link StringRedisTemplate} bean for interacting with Redis.
     * This template provides a convenient way to work with Redis data using String keys and values.
     *
     * @param connectionFactory the Redis connection factory used to establish a connection to the Redis server.
     * @return a configured {@link StringRedisTemplate} instance for string-based Redis operations.
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}