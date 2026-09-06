package com.fleettrack.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestCacheConfig {

    @Bean
    CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CacheConfig.VEHICLE_SUMMARY_CACHE,
                CacheConfig.DRIVER_SUMMARY_CACHE
        );
    }
}
