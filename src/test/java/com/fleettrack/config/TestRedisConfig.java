package com.fleettrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestRedisConfig {

    @Bean
    @SuppressWarnings("unchecked")
    RedisTemplate<String, String> redisTemplate() {
        return mock(RedisTemplate.class);
    }
}
