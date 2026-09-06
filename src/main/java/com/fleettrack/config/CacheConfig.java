package com.fleettrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {

    public static final String VEHICLE_SUMMARY_CACHE = "vehicleSummaries";
    public static final String DRIVER_SUMMARY_CACHE = "driverSummaries";

    @Bean
    RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${fleettrack.cache.vehicle-summary-ttl:300}") long vehicleTtl,
            @Value("${fleettrack.cache.driver-summary-ttl:300}") long driverTtl
    ) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = Map.of(
                VEHICLE_SUMMARY_CACHE, defaultConfig.entryTtl(Duration.ofSeconds(vehicleTtl)),
                DRIVER_SUMMARY_CACHE, defaultConfig.entryTtl(Duration.ofSeconds(driverTtl))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofSeconds(vehicleTtl)))
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
