package pl.exceptionhandled.repetitio.auth.infrastructure;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.TimeMeter;

import java.time.Duration;

public class AuthRateLimiter {
    private final TimeMeter timeMeter;
    private final AuthRateLimitProperties properties;

    public AuthRateLimiter(AuthRateLimitProperties properties) {
        this(properties, TimeMeter.SYSTEM_MILLISECONDS);
    }

    AuthRateLimiter(AuthRateLimitProperties properties, TimeMeter timeMeter) {
        this.properties = properties;
        this.timeMeter = timeMeter;
    }

    private final Cache<String, Bucket> buckets =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterAccess(Duration.ofMinutes(20))
                    .build();

    public boolean allow(String clientKey) {
        Bucket bucket = buckets.get(clientKey, key ->
                Bucket.builder()
                        .withCustomTimePrecision(timeMeter)
                        .addLimit(limit -> limit
                                .capacity(properties.capacity())
                                .refillGreedy(properties.refillTokens(), properties.refillPeriod()))
                        .build()
        );

        return bucket.tryConsume(1);
    }
}
