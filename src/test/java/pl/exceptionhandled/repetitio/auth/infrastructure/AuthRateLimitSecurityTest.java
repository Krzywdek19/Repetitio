package pl.exceptionhandled.repetitio.auth.infrastructure;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.FilterChainProxy;
import pl.exceptionhandled.repetitio.shared.security.SecurityConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitSecurityTest {
    @Test
    void shouldRegisterFilterOnceAndUseConfiguredLimitInSecurityChain() {
        new WebApplicationContextRunner()
                .withUserConfiguration(AuthRateLimitConfiguration.class, SecurityConfiguration.class, TestSecurity.class)
                .withPropertyValues(
                        "app.rate-limit.login.capacity=3",
                        "app.rate-limit.login.refill-tokens=2",
                        "app.rate-limit.login.refill-period=1h")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    // The filter belongs only to Spring Security, not servlet component registration.
                    assertThat(context).doesNotHaveBean(AuthLimitFilter.class);
                    var proxy = context.getBean("springSecurityFilterChain", FilterChainProxy.class);
                    assertThat(proxy.getFilterChains().getFirst().getFilters())
                            .filteredOn(AuthLimitFilter.class::isInstance).hasSize(1);
                    var reachedEndpoint = new AtomicInteger();
                    FilterChain endpoint = (request, response) -> reachedEndpoint.incrementAndGet();
                    for (int i = 0; i < 4; i++) {
                        var request = new MockHttpServletRequest(context.getServletContext(), "POST", "/api/auth/login");
                        request.setServletPath("/api/auth/login");
                        request.setRemoteAddr("127.0.0.1");
                        var response = new MockHttpServletResponse();
                        proxy.doFilter(request, response, endpoint);
                        assertThat(response.getStatus()).isEqualTo(i < 3 ? 200 : 429);
                    }
                    assertThat(reachedEndpoint.get()).isEqualTo(3);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class TestSecurity {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> { throw new BadJwtException("No JWT needed for public login endpoint"); };
        }
    }
}
