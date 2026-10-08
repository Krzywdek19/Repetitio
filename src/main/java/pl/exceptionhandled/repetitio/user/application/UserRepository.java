package pl.exceptionhandled.repetitio.user.application;
import java.util.Optional;
public interface UserRepository {
    boolean existsByEmail(String email);
    void insert(UserIdentity user, String passwordHash);
    Optional<AuthenticationCredentials> credentials(String email);
}

