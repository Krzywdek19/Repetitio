package pl.exceptionhandled.repetitio.user.application;

import java.time.Clock;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final Clock clock;

    @Transactional
    public UserIdentity register(String email, String passwordHash) {
        if (repository.existsByEmail(email)) throw new EmailAlreadyRegisteredException();
        var user = new UserIdentity(UUID.randomUUID(), email, clock.instant());
        repository.insert(user, passwordHash);
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<AuthenticationCredentials> credentials(String email) { return repository.credentials(email); }
}

