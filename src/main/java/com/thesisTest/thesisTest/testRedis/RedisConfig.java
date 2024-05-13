package com.thesisTest.thesisTest.testRedis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key와 Value Serializer 설정
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));

        // HashKey와 HashValue Serializer 설정 (필요한 경우)
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericToStringSerializer<>(Integer.class));

        return template;
    }
}
