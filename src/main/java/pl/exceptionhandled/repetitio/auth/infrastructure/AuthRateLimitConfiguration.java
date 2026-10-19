package pl.exceptionhandled.repetitio.auth.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthRateLimitProperties.class)
public class AuthRateLimitConfiguration {

    @Bean
    AuthRateLimiter authRateLimiter(AuthRateLimitProperties properties) {
        return new AuthRateLimiter(properties);
    }
}
