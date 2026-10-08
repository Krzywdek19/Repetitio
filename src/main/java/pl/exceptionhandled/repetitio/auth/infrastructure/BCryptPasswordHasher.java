package pl.exceptionhandled.repetitio.auth.infrastructure;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.exceptionhandled.repetitio.auth.application.PasswordHasher;

@Component
class BCryptPasswordHasher implements PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    public String hash(String password) { return encoder.encode(password); }
    public boolean matches(String password, String hash) { return encoder.matches(password, hash); }
}

