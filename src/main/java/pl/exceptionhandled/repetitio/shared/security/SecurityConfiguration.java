package pl.exceptionhandled.repetitio.shared.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import pl.exceptionhandled.repetitio.auth.infrastructure.AuthLimitFilter;
import pl.exceptionhandled.repetitio.auth.infrastructure.AuthRateLimiter;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthRateLimiter limiter) throws Exception {
        return http
                .addFilterBefore(new AuthLimitFilter(limiter), AuthorizationFilter.class)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .requestCache(cache -> cache.disable())
                .addFilterAfter(new RequestSizeFilter(), AuthorizationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> {})
                        .authenticationEntryPoint((request, response, exception) -> problem(response, 401))
                        .accessDeniedHandler((request, response, exception) -> problem(response, 403)))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> problem(response, 401))
                        .accessDeniedHandler((request, response, exception) -> problem(response, 403)))
                .build();
    }

    private static void problem(HttpServletResponse response, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        if (status == 401) response.setHeader("WWW-Authenticate", "Bearer");
        String title = status == 401 ? "Unauthorized" : "Forbidden";
        response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"" + title
                + "\",\"status\":" + status + ",\"detail\":\"Access denied\"}");
    }
}

