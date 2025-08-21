package com.integration.redisdockerizer.session.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * RedisSessionConfig class provides the configuration for Redis-based session management.
 * It enables the usage of Redis to store and manage HTTP session data in a Spring Boot application.
 * <p>
 * The class is annotated with {@link EnableRedisHttpSession}, which ensures that Spring Session will use Redis
 * to store session data instead of the default in-memory session storage.
 * </p>
 *
 * <p>
 * This configuration provides a RedisTemplate configured for session storage, where both keys and values are serialized as strings.
 * This RedisTemplate is used by the Spring Session mechanism to store and retrieve session-related data from Redis.
 * </p>
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *     <li>Enables Redis as the store for HTTP sessions using Spring Session.</li>
 *     <li>Configures a RedisTemplate that serializes session data as strings.</li>
 *     <li>Supports TTL (Time-To-Live) for session expiration, managed by Redis.</li>
 * </ul>
 */
@Configuration
@EnableRedisHttpSession
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

        return template;
    }
}