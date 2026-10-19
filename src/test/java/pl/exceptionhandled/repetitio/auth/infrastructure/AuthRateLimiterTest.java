package pl.exceptionhandled.repetitio.auth.infrastructure;

import io.github.bucket4j.TimeMeter;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AuthRateLimiterTest {

    private final AuthRateLimitProperties properties =
            new AuthRateLimitProperties(5, 5, Duration.ofMinutes(1));
    private final AuthRateLimiter limiter = new AuthRateLimiter(properties, new FixedTimeMeter());

    @Test
    void shouldRejectRequestWhenBucketIsEmpty() {
        String clientKey = "client1";

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow(clientKey));
        }

        assertFalse(limiter.allow(clientKey));
    }

    @Test
    void shouldMaintainSeparateBucketsForDifferentClients() {
        String clientKey1 = "client1";
        String clientKey2 = "client2";

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow(clientKey1));
        }

        assertFalse(limiter.allow(clientKey1));
        assertTrue(limiter.allow(clientKey2));
    }

    @Test
    void shouldAllowOnlyFiveConcurrentRequestsForSameClient() throws InterruptedException{
        String clientKey = "client1";

        CountDownLatch latch = new CountDownLatch(100);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(100);
        AtomicInteger allowed = new AtomicInteger(0);
        AtomicInteger rejected = new AtomicInteger(0);

        Runnable task = () -> {
            latch.countDown();

            try {
                start.await();
                if(limiter.allow(clientKey)) {
                    allowed.incrementAndGet();
                }else {
                    rejected.incrementAndGet();
                }
            }catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Worker interrupted", e);
            } finally {
                end.countDown();
            }
        };

        try (ExecutorService executorService = Executors.newFixedThreadPool(100)) {

        for (int i = 0; i < 100; i++) {
            executorService.submit(task);
        }

        try {
            assertThat(latch.await(15, TimeUnit.SECONDS)).isTrue();
        } finally {
            start.countDown();
        }

        assertThat(end.await(15, TimeUnit.SECONDS)).isTrue();
        assertEquals(5, allowed.get());
        assertEquals(95, rejected.get());

        }
    }

    @Test
    void shouldRefillTokensAfterOneMinute() {
        MutableTimeMeter clock = new MutableTimeMeter();
        AuthRateLimiter limiter = new AuthRateLimiter(properties, clock);

        String clientKey = "client1";

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow(clientKey));
        }

        assertFalse(limiter.allow(clientKey));

        clock.advance(Duration.ofMinutes(1));

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow(clientKey));
        }
    }

    @Test
    void shouldRefillOneTokenAfterTwelveSeconds() {
        MutableTimeMeter clock = new MutableTimeMeter();
        AuthRateLimiter limiter = new AuthRateLimiter(properties, clock);

        String clientKey = "client1";

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allow(clientKey));
        }

        assertFalse(limiter.allow(clientKey));

        clock.advance(Duration.ofSeconds(11));

        assertFalse(limiter.allow(clientKey));

        clock.advance(Duration.ofSeconds(1));

        assertTrue(limiter.allow(clientKey));
        assertFalse(limiter.allow(clientKey));
    }

    @Test
    void shouldUseCustomCapacityRefillTokensAndPeriod() {
        MutableTimeMeter clock = new MutableTimeMeter();
        AuthRateLimiter limiter = new AuthRateLimiter(
                new AuthRateLimitProperties(3, 2, Duration.ofSeconds(10)), clock);

        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allow("client"));
        }
        assertFalse(limiter.allow("client"));
        clock.advance(Duration.ofSeconds(4));
        assertFalse(limiter.allow("client"));
        clock.advance(Duration.ofSeconds(1));
        assertTrue(limiter.allow("client"));
        assertFalse(limiter.allow("client"));

        clock.advance(Duration.ofMinutes(1));
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allow("client"));
        }
        assertFalse(limiter.allow("client"));
    }

    private static class FixedTimeMeter implements TimeMeter {
        @Override
        public long currentTimeNanos() {
            return 1_000_000_000_000L;
        }

        @Override
        public boolean isWallClockBased() {
            return false;
        }
    }

    private static class MutableTimeMeter implements TimeMeter {

        private final AtomicLong now =
                new AtomicLong(1_000_000_000_000L);

        @Override
        public long currentTimeNanos() {
            return now.get();
        }

        @Override
        public boolean isWallClockBased() {
            return false;
        }

        void advance(Duration duration) {
            now.addAndGet(duration.toNanos());
        }
    }
}
