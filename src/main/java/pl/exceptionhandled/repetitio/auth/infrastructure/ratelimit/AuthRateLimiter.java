package pl.exceptionhandled.repetitio.auth.infrastructure.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.TimeMeter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.repetitio.auth.config.RateLimitProperties;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class AuthRateLimiter {
    private final TimeMeter timeMeter;
    private final RateLimitProperties properties;
    private final Cache<String, Bucket> buckets;

    @Autowired
    public AuthRateLimiter(RateLimitProperties properties) {
        this(properties, TimeMeter.SYSTEM_MILLISECONDS);
    }

    AuthRateLimiter(
            RateLimitProperties properties,
            TimeMeter timeMeter
    ) {
        this.properties = properties;
        this.timeMeter = timeMeter;

        this.buckets = Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .build();
    }

    public RateLimitDecision consumeLogin(String clientIp) {
        return consume(
                "login:" + clientIp,
                properties.login(),
                true
        );
    }

    public RateLimitDecision consumeRegistration(String clientIp) {
        return consume(
                "register:" + clientIp,
                properties.register(),
                false
        );
    }

    private RateLimitDecision consume(
            String key,
            RateLimitProperties.Limit policy,
            boolean greedy
    ) {
        Bucket bucket = buckets.get(
                key,
                ignored -> createBucket(policy, greedy)
        );

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        long retryAfterSeconds = 0;

        if (!probe.isConsumed()) {
            long nanos = probe.getNanosToWaitForRefill();

            retryAfterSeconds = TimeUnit.NANOSECONDS.toSeconds(nanos);

            if (nanos % 1_000_000_000L != 0) {
                retryAfterSeconds++;
            }
        }

        return new RateLimitDecision(
                probe.isConsumed(),
                probe.getRemainingTokens(),
                retryAfterSeconds
        );
    }

    private Bucket createBucket(
            RateLimitProperties.Limit policy,
            boolean greedy
    ) {
        if (greedy) {
            return Bucket.builder()
                    .addLimit(limit -> limit
                            .capacity(policy.capacity())
                            .refillGreedy(
                                    policy.refillTokens(),
                                    policy.refillPeriod()
                            ))
                    .withCustomTimePrecision(timeMeter)
                    .build();
        }

        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(policy.capacity())
                        .refillIntervally(
                                policy.refillTokens(),
                                policy.refillPeriod()
                        ))
                .withCustomTimePrecision(timeMeter)
                .build();
    }

    public record RateLimitDecision(
            boolean allowed,
            long remainingTokens,
            long retryAfterSeconds
    ) {}
}