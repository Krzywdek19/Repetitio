package pl.exceptionhandled.repetitio.auth.infrastructure.ratelimit;

import io.github.bucket4j.TimeMeter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.exceptionhandled.repetitio.auth.config.RateLimitProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class AuthRateLimiterTest {

    private AuthRateLimiter limiter;
    private MutableTimeMeter timeMeter;

    @BeforeEach
    void setUp() {
        timeMeter = new MutableTimeMeter();

        var loginPolicy = new RateLimitProperties.Limit(
                5,
                1,
                Duration.ofSeconds(12)
        );

        var registerPolicy = new RateLimitProperties.Limit(
                5,
                5,
                Duration.ofMinutes(1)
        );

        var properties = new RateLimitProperties(
                loginPolicy,
                registerPolicy
        );

        limiter = new AuthRateLimiter(properties, timeMeter);
    }

    @Test
    void shouldRejectWhenBucketIsEmpty() {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.consumeLogin("client-1").allowed());
        }

        var decision = limiter.consumeLogin("client-1");

        assertFalse(decision.allowed());
        assertEquals(0, decision.remainingTokens());
        assertEquals(12, decision.retryAfterSeconds());
    }

    @Test
    void shouldKeepClientBucketsIndependent() {
        exhaustLoginBucket("client-1");

        assertFalse(limiter.consumeLogin("client-1").allowed());
        assertTrue(limiter.consumeLogin("client-2").allowed());
    }

    @Test
    void shouldKeepEndpointBucketsIndependent() {
        exhaustLoginBucket("client-1");

        assertFalse(limiter.consumeLogin("client-1").allowed());

        assertTrue(
                limiter.consumeRegistration("client-1").allowed()
        );
    }

    @Test
    void shouldRefillLoginBucket() {
        exhaustLoginBucket("client-1");

        assertFalse(limiter.consumeLogin("client-1").allowed());

        timeMeter.advance(Duration.ofSeconds(12));

        assertTrue(limiter.consumeLogin("client-1").allowed());

        assertFalse(limiter.consumeLogin("client-1").allowed());
    }

    @Test
    void shouldRefillRegistrationBucket() {
        for (int i = 0; i < 5; i++) {
            assertTrue(
                    limiter.consumeRegistration("client-1").allowed()
            );
        }

        assertFalse(limiter.consumeRegistration("client-1").allowed());

        timeMeter.advance(Duration.ofSeconds(59));

        assertFalse(limiter.consumeRegistration("client-1").allowed());

        timeMeter.advance(Duration.ofSeconds(1));

        for (int i = 0; i < 5; i++) {
            assertTrue(
                    limiter.consumeRegistration("client-1").allowed()
            );
        }
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        int requestCount = 100;

        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Boolean>> results = new ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                results.add(executor.submit(() -> {
                    start.await();

                    return limiter.consumeLogin("same-client").allowed();
                }));
            }

            start.countDown();

            int allowed = 0;

            for (Future<Boolean> result : results) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    allowed++;
                }
            }

            assertEquals(5, allowed);

        } finally {
            executor.shutdownNow();
        }
    }

    private void exhaustLoginBucket(String client) {
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.consumeLogin(client).allowed());
        }
    }

    private static class MutableTimeMeter implements TimeMeter {

        private final AtomicLong currentNanos = new AtomicLong();

        @Override
        public long currentTimeNanos() {
            return currentNanos.get();
        }

        @Override
        public boolean isWallClockBased() {
            return false;
        }

        void advance(Duration duration) {
            currentNanos.addAndGet(duration.toNanos());
        }
    }
}