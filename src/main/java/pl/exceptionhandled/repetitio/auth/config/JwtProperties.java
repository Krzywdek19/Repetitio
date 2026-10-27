package pl.exceptionhandled.repetitio.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties (
        String issuer,
        Duration ttl,
        String secretBase64
){
}
