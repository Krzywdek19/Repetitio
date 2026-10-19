package pl.exceptionhandled.repetitio.auth.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRateLimitConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AuthRateLimitConfiguration.class);

    @Test
    void shouldBindAndInjectCustomConfiguration() {
        configuredRunner().run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(AuthRateLimitProperties.class)
                    .hasSingleBean(AuthRateLimiter.class);
            var properties = context.getBean(AuthRateLimitProperties.class);
            assertThat(properties.capacity()).isEqualTo(3);
            assertThat(properties.refillTokens()).isEqualTo(2);
            assertThat(properties.refillPeriod()).isEqualTo(Duration.ofHours(1));

            var limiter = context.getBean(AuthRateLimiter.class);
            for (int i = 0; i < 3; i++) {
                assertThat(limiter.allow("client")).isTrue();
            }
            assertThat(limiter.allow("client")).isFalse();
        });
    }

    @Test
    void shouldBindCheckedInYamlConfiguration() throws IOException {
        var yaml = new YamlPropertySourceLoader().load("application",
                new ClassPathResource("application.yaml")).getFirst();
        runner.withInitializer(context -> context.getEnvironment().getPropertySources().addLast(yaml))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(AuthRateLimitProperties.class))
                            .isEqualTo(new AuthRateLimitProperties(5, 5, Duration.ofMinutes(1)));
                });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "capacity=0", "capacity=-1", "refill-tokens=0", "refill-tokens=-1",
            "refill-period=0s", "refill-period=-1s"
    })
    void shouldRejectNonPositiveConfigurationAtStartup(String property) {
        configuredRunner().withPropertyValues("app.rate-limit.login." + property).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
        });
    }

    @Test
    void shouldRejectMissingConfigurationAtStartup() {
        runner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
        });
    }

    @Test
    void shouldRejectMissingPeriodAtStartup() {
        runner.withPropertyValues("app.rate-limit.login.capacity=3", "app.rate-limit.login.refill-tokens=2")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                });
    }

    @Test
    void shouldRejectMalformedPeriodAtStartup() {
        configuredRunner().withPropertyValues("app.rate-limit.login.refill-period=invalid").run(context -> {
            assertThat(context).hasFailed();
        });
    }

    private ApplicationContextRunner configuredRunner() {
        return runner.withPropertyValues(
                "app.rate-limit.login.capacity=3",
                "app.rate-limit.login.refill-tokens=2",
                "app.rate-limit.login.refill-period=1h");
    }
}
