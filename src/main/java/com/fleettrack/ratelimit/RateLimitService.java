package com.fleettrack.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${fleettrack.rate-limit.capacity:100}")
    private long capacity;

    @Value("${fleettrack.rate-limit.refill-tokens:100}")
    private long refillTokens;

    @Value("${fleettrack.rate-limit.refill-duration-minutes:1}")
    private long refillDurationMinutes;

    public void checkRateLimit(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, this::createBucket);
        if (!bucket.tryConsume(1)) {
            throw new com.fleettrack.exception.RateLimitExceededException(
                    "Rate limit exceeded. Maximum %d requests per %d minute(s).".formatted(capacity, refillDurationMinutes)
            );
        }
    }

    private Bucket createBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofMinutes(refillDurationMinutes))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
