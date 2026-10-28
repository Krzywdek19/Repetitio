package pl.exceptionhandled.repetitio.auth.application;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoginCredentialVerifierTest {

    private final PasswordEncoder passwordEncoder =
            mock(PasswordEncoder.class);

    @Test
    void shouldCheckPasswordEvenWhenUserDoesNotExist() {
        when(passwordEncoder.encode(anyString()))
                .thenReturn("dummy-hash");

        var verifier = new LoginCredentialVerifier(passwordEncoder);

        boolean result = verifier.isValid(
                Optional.empty(),
                "WrongPassword123!"
        );

        assertFalse(result);

        verify(passwordEncoder).matches(
                "WrongPassword123!",
                "dummy-hash"
        );
    }

    @Test
    void shouldRejectMissingUserEvenIfDummyPasswordMatches() {
        when(passwordEncoder.encode(anyString()))
                .thenReturn("dummy-hash");

        when(passwordEncoder.matches(
                "password",
                "dummy-hash"
        )).thenReturn(true);

        var verifier = new LoginCredentialVerifier(passwordEncoder);

        boolean result = verifier.isValid(
                Optional.empty(),
                "password"
        );

        assertFalse(result);
    }
}