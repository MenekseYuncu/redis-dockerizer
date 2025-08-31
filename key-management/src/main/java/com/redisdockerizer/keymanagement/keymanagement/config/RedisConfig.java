package com.redisdockerizer.keymanagement.keymanagement.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for setting up RedisTemplate.
 * This class configures the RedisTemplate to interact with Redis,
 * setting up the necessary serializers for keys and values.
 */
@Configuration
public class RedisConfig {

    /**
     * Specifies the Redis server host address used for connecting to the Redis instance.
     * The value is injected from the application properties using the
     * "spring.data.redis.host" configuration key. If not provided, defaults to "localhost".
     */
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    /**
     * Represents the Redis port configuration property.
     * This value is used to connect to the Redis server and is typically
     * specified in the application's configuration properties under the key
     * "spring.data.redis.port". If not explicitly configured, the default value
     * of 6379 is used.
     */
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Creates and provides a LettuceConnectionFactory bean for connecting to the Redis server.
     * The connection factory is configured using the host and port specified in the application's
     * configuration properties.
     *
     * @return a LettuceConnectionFactory instance configured with the specified Redis host and port
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }


    /**
     * Configures and provides a bean for RedisTemplate, which is used to perform operations
     * on Redis. The template is configured with appropriate serializers for keys, hash keys,
     * values, and hash values to ensure proper data formatting and serialization.
     *
     * @param connectionFactory the RedisConnectionFactory that establishes the connection to Redis
     * @param objectMapper      the ObjectMapper used to configure the JSON serialization for values
     * @return a configured RedisTemplate instance ready for use
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.setDefaultSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }
}