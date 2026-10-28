package pl.exceptionhandled.repetitio.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.repetitio.auth.api.LoginRequest;
import pl.exceptionhandled.repetitio.auth.api.LoginResponse;
import pl.exceptionhandled.repetitio.auth.api.RegisterRequest;
import pl.exceptionhandled.repetitio.auth.api.RegisterResponse;
import pl.exceptionhandled.repetitio.auth.domain.exception.EmailAlreadyRegisteredException;
import pl.exceptionhandled.repetitio.auth.domain.exception.InvalidCredentialsException;
import pl.exceptionhandled.repetitio.auth.domain.password.PasswordPolicy;
import pl.exceptionhandled.repetitio.auth.infrastructure.security.JwtTokenService;
import pl.exceptionhandled.repetitio.users.domain.User;
import pl.exceptionhandled.repetitio.users.domain.UserRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final LoginCredentialVerifier credentialVerifier;
    private final PasswordPolicy passwordPolicy;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        passwordPolicy.validate(request.password());

        String email = request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = userRepository.save(
                new User(email, passwordHash)
        );

        return new RegisterResponse(
                user.getId(),
                user.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        var email = request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        var user = userRepository.findByEmailIgnoreCase(email);

        if (!credentialVerifier.isValid(user, request.password())) {
            throw new InvalidCredentialsException();
        }

        return jwtTokenService.generate(user.orElseThrow());
    }
}
