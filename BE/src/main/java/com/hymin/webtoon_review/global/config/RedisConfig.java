package com.hymin.webtoon_review.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration();

        redisConfiguration.setHostName(host);
        redisConfiguration.setPort(port);
        redisConfiguration.setPassword(password);

        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<?, ?> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
        redisTemplate.setHashValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));

        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        return container;
    }

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
        RedisConnectionFactory connectionFactory
    ) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .batchSize(10)
                .pollTimeout(Duration.ofSeconds(1))
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
            StreamMessageListenerContainer.create(connectionFactory, options);

        container.start();

        return container;
    }
}
