package com.redisdockerizer.caching.caching.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * RedisCacheConfig class provides the configuration for setting up Redis as a cache manager in a Spring application.
 * It enables caching using the @EnableCaching annotation and configures Redis as the underlying caching mechanism.
 * <p>
 * This configuration utilizes a RedisConnectionFactory to create a CacheManager that manages caching operations
 * with Redis. It sets up Redis to store data in a serialized JSON format and defines a default Time-To-Live (TTL)
 * of 5 minutes for cache entries. Additionally, it ensures that null values are not cached.
 *
 * @see org.springframework.cache.CacheManager
 * @see org.springframework.data.redis.cache.RedisCacheManager
 * @see org.springframework.data.redis.connection.RedisConnectionFactory
 */
@Configuration
public class RedisCacheConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * Creates a {@link LettuceConnectionFactory} bean for establishing a connection to the Redis server.
     * It is configured using the provided host and port values.
     *
     * @return a configured {@link LettuceConnectionFactory} instance used to interact with the Redis server.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    /**
     * Creates a RedisTemplate bean used for performing Redis operations in a Spring application.
     * The RedisTemplate is configured with serializers for keys, hash keys, values, and hash values.
     *
     * @param connectionFactory the RedisConnectionFactory used to establish the connection with the Redis server.
     * @return a configured RedisTemplate instance for interacting with Redis.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates a CacheManager bean backed by Redis for managing caching operations.
     * <p>
     * - Data will be serialized in JSON format using the GenericJackson2JsonRedisSerializer.
     * - The default TTL (Time-To-Live) for cache entries is set to 5 minutes.
     * - Caching of null values is disabled.
     *
     * @param connectionFactory RedisConnectionFactory used to connect to the Redis server.
     * @return RedisCacheManager configured for Redis-based caching operations.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer())
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}
