package com.menekse.redisdockerizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for setting up RedisTemplate.
 * This class configures the RedisTemplate to interact with Redis,
 * setting up the necessary serializers for keys and values.
 */
@Configuration
public class RedisConfig {

    /**
     * Creates and configures a RedisTemplate bean for performing Redis operations.
     * The template is configured to use String-based keys and values by setting
     * both key and value serializers to StringRedisSerializer.
     *
     * @param connectionFactory The Redis connection factory, used to establish the connection to Redis.
     * @return A configured RedisTemplate for general data operations with Redis.
     * It supports String-based key and value serialization.
     */
    @Bean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}