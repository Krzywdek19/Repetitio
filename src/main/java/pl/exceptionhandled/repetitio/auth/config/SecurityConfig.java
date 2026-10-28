package pl.exceptionhandled.repetitio.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import pl.exceptionhandled.repetitio.auth.infrastructure.ratelimit.AuthRateLimitFilter;
import pl.exceptionhandled.repetitio.auth.infrastructure.ratelimit.AuthRateLimiter;
import pl.exceptionhandled.repetitio.shared.api.ApiPaths;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthRateLimiter limiter, JsonMapper jsonMapper) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, ApiPaths.AUTH_REGISTER, ApiPaths.AUTH_LOGIN)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()))
                .addFilterBefore(new AuthRateLimitFilter(limiter, jsonMapper), AuthorizationFilter.class)
                .build();
    }
}
