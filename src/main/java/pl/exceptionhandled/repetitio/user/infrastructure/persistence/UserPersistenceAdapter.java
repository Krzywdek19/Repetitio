package pl.exceptionhandled.repetitio.user.infrastructure.persistence;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
import pl.exceptionhandled.repetitio.user.application.*;

@Repository
@RequiredArgsConstructor
class UserPersistenceAdapter implements UserRepository {
    private final JpaUserRepository repository;
    public boolean existsByEmail(String email) { return repository.existsByEmail(email); }
    public void insert(UserIdentity user, String hash) {
        try {
            repository.saveAndFlush(new UserEntity(user.id(), user.email(), hash, user.createdAt()));
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_user_email".equals(violation.getConstraintName())) {
                    // Unwind the transaction immediately; do not continue using it after the failed flush.
                    throw new EmailAlreadyRegisteredException();
                }
            }
            throw exception;
        }
    }
    public Optional<AuthenticationCredentials> credentials(String email) {
        return repository.findByEmail(email).map(u -> new AuthenticationCredentials(u.getId(), u.getPasswordHash()));
    }
}

