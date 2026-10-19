package pl.exceptionhandled.repetitio.auth.infrastructure;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.rate-limit.login")
public record AuthRateLimitProperties(
        @Positive long capacity,
        @Positive long refillTokens,
        @NotNull Duration refillPeriod) {

    @AssertTrue(message = "refill-period must be positive")
    public boolean isRefillPeriodPositive() {
        return refillPeriod == null || refillPeriod.isPositive();
    }
}
