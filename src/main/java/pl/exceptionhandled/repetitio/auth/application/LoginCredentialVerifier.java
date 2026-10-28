package pl.exceptionhandled.repetitio.auth.application;

import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.repetitio.users.domain.User;

import java.util.Optional;
import java.util.UUID;

@Component
public class LoginCredentialVerifier {
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;

    public LoginCredentialVerifier(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.dummyPasswordHash = passwordEncoder.encode(
                UUID.randomUUID().toString()
        );
    }

    public boolean isValid(Optional<User> user, String rawPassword) {
        String passwordHash = user
                .map(User::getPasswordHash)
                .orElse(dummyPasswordHash);

        boolean matches = passwordEncoder.matches(
                rawPassword, passwordHash
        );

        return user.isPresent() && matches;
    }
}
