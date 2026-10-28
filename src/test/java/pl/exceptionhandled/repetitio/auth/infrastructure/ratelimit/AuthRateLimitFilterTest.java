package pl.exceptionhandled.repetitio.auth.infrastructure.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import pl.exceptionhandled.repetitio.shared.api.ApiPaths;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthRateLimitFilterTest {

    private static final String CLIENT_IP = "192.0.2.10";

    private AuthRateLimiter limiter;
    private AuthRateLimitFilter filter;

    @BeforeEach
    void setUp() {
        limiter = mock(AuthRateLimiter.class);

        filter = new AuthRateLimitFilter(
                limiter,
                JsonMapper.builder().build()
        );
    }

    @Test
    void shouldAllowRequestWhenTokenIsAvailable() throws Exception {
        when(limiter.consumeLogin(CLIENT_IP))
                .thenReturn(new AuthRateLimiter.RateLimitDecision(
                        true, 4, 0
                ));

        var request = loginRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertSame(request, chain.getRequest());

        verify(limiter).consumeLogin(CLIENT_IP);
    }

    @Test
    void shouldRejectRequestWhenLimitIsExceeded() throws Exception {
        when(limiter.consumeLogin(CLIENT_IP))
                .thenReturn(new AuthRateLimiter.RateLimitDecision(
                        false, 0, 12
                ));

        var request = loginRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
        assertEquals("12", response.getHeader("Retry-After"));

        assertNull(chain.getRequest());

        assertTrue(
                response.getContentAsString()
                        .contains("RATE_LIMIT_EXCEEDED")
        );
    }

    @Test
    void shouldApplySeparateLimitToRegistration() throws Exception {
        when(limiter.consumeRegistration(CLIENT_IP))
                .thenReturn(new AuthRateLimiter.RateLimitDecision(
                        false, 0, 60
                ));

        var request = new MockHttpServletRequest(
                "POST", ApiPaths.AUTH_REGISTER
        );

        request.setServletPath(ApiPaths.AUTH_REGISTER);
        request.setRemoteAddr(CLIENT_IP);

        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
        assertNull(chain.getRequest());

        verify(limiter).consumeRegistration(CLIENT_IP);
        verify(limiter, never()).consumeLogin(anyString());
    }

    @Test
    void shouldIgnoreUnprotectedEndpoints() throws Exception {
        var request = new MockHttpServletRequest(
                "GET", "/api/v1/notes"
        );

        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertSame(request, chain.getRequest());
        verifyNoInteractions(limiter);
    }

    private MockHttpServletRequest loginRequest() {
        var request = new MockHttpServletRequest(
                "POST", ApiPaths.AUTH_LOGIN
        );

        request.setServletPath(ApiPaths.AUTH_LOGIN);
        request.setRemoteAddr(CLIENT_IP);

        return request;
    }
}