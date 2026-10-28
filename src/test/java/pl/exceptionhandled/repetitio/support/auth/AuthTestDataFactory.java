package pl.exceptionhandled.repetitio.support.auth;

import pl.exceptionhandled.repetitio.auth.api.RegisterRequest;

import java.util.UUID;

public final class AuthTestDataFactory {

    public static final String VALID_PASSWORD = "VeryStrongPassword123!";

    private AuthTestDataFactory() {
    }

    public static String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    public static RegisterRequest validRegistration() {
        return new RegisterRequest(uniqueEmail(), VALID_PASSWORD);
    }
}
