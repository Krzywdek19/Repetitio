package pl.exceptionhandled.repetitio.auth.application;

import pl.exceptionhandled.repetitio.users.domain.User;

public sealed interface RotationResult {
    record Success(
            User user,
            String refreshToken
    ) implements RotationResult {}

    record Rejected(
            boolean reuseDetected
    ) implements RotationResult {}
}
