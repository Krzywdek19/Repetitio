package pl.exceptionhandled.repetitio.auth.infrastructure;

import jakarta.servlet.ServletException;
import io.github.bucket4j.TimeMeter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class AuthRateLimitFilterTest {
    @ParameterizedTest
    @ValueSource(ints = {5, 3})
    void shouldReturn429WhenLoginRateLimitIsExceeded(int capacity) throws ServletException, IOException {
        AuthRateLimiter limiter = limiter(capacity);
        AuthLimitFilter filter = new AuthLimitFilter(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        request.setServletPath("/api/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        for (int i = 0; i < capacity; i++) {
            assertTrue(limiter.allow("login:127.0.0.1"));
        }

        filter.doFilter(request, response, chain);
        assertEquals(429, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void shouldPassRequestWhenLimitIsNotExceeded() throws ServletException, IOException {
        AuthRateLimiter limiter = limiter(5);
        AuthLimitFilter filter = new AuthLimitFilter(limiter);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");

        request.setServletPath("/api/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertSame(request, chain.getRequest());
    }

    @Test
    void shouldNotLimitOtherMethodsOrPaths() throws ServletException, IOException {
        AuthRateLimiter limiter = limiter(3);
        AuthLimitFilter filter = new AuthLimitFilter(limiter);
        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allow("login:127.0.0.1"));
        }
        for (String[] endpoint : new String[][]{
                {"GET", "/api/auth/login"}, {"POST", "/api/auth/register"}}) {
            MockHttpServletRequest request = new MockHttpServletRequest(endpoint[0], endpoint[1]);
            request.setServletPath(endpoint[1]);
            request.setRemoteAddr("127.0.0.1");
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(request, new MockHttpServletResponse(), chain);
            assertSame(request, chain.getRequest());
        }
    }

    private AuthRateLimiter limiter(int capacity) {
        return new AuthRateLimiter(new AuthRateLimitProperties(capacity, capacity, Duration.ofMinutes(1)),
                new TimeMeter() {
                    @Override public long currentTimeNanos() { return 0; }
                    @Override public boolean isWallClockBased() { return false; }
                });
    }
}
