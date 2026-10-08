package pl.exceptionhandled.repetitio.auth.application;

import java.util.Locale;
import org.springframework.stereotype.Service;
import pl.exceptionhandled.repetitio.user.application.*;
import pl.exceptionhandled.repetitio.shared.error.ApiException;

@Service
public class AuthService {
    private final UserService users;
    private final PasswordHasher passwords;
    private final AccessTokenIssuer tokens;
    private final String dummyHash;

    public AuthService(UserService users, PasswordHasher passwords, AccessTokenIssuer tokens) {
        this.users = users; this.passwords = passwords; this.tokens = tokens;
        this.dummyHash = passwords.hash(java.util.UUID.randomUUID().toString());
    }

    public UserIdentity register(String email, String password) {
        String hash = passwords.hash(password);
        try {
            // UserService's transaction has rolled back before this catch runs.
            return users.register(normalize(email), hash);
        } catch (EmailAlreadyRegisteredException exception) {
            throw new ApiException(409, "Email already registered");
        }
    }

    public TokenResponse login(String email, String password) {
        var credentials = users.credentials(normalize(email));
        boolean matches = passwords.matches(password, credentials.map(AuthenticationCredentials::passwordHash).orElse(dummyHash));
        if (credentials.isEmpty() || !matches) throw new ApiException(401, "Invalid email or password");
        return tokens.issue(credentials.get().id());
    }

    private static String normalize(String email) { return email.strip().toLowerCase(Locale.ROOT); }
}

