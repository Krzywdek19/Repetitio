package pl.exceptionhandled.repetitio.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.auth.rate-limit")
public record RateLimitProperties(

        @Valid @NotNull
        Limit login,

        @Valid @NotNull
        Limit register

) {

    public record Limit(

            @Min(1)
            long capacity,

            @Min(1)
            long refillTokens,

            @NotNull
            Duration refillPeriod

    ) {}
}