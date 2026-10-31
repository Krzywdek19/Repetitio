package pl.exceptionhandled.repetitio.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.repetitio.auth.domain.token.RefreshToken;
import pl.exceptionhandled.repetitio.auth.domain.token.RefreshTokenRepository;
import pl.exceptionhandled.repetitio.auth.infrastructure.security.RefreshTokenCodec;
import pl.exceptionhandled.repetitio.users.domain.User;
import pl.exceptionhandled.repetitio.users.domain.UserRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_TTL =
            Duration.ofDays(14);

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCodec refreshTokenCodec;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public String issue(User user) {
        Instant now = clock.instant();

        return createToken(
                user,
                UUID.randomUUID(),
                now,
                now.plus(REFRESH_TOKEN_TTL)
        );
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return reject();
        }

        String tokenHash = refreshTokenCodec.hash(rawToken);

        var userId = refreshTokenRepository
                .findUserIdByTokenHash(tokenHash);

        if (userId.isEmpty()) {
            return reject();
        }

        if (userRepository.findByIdForUpdate(userId.get()).isEmpty()) {
            return reject();
        }

        var tokenOptional = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash);

        if (tokenOptional.isEmpty()) {
            return reject();
        }

        RefreshToken currentToken = tokenOptional.get();
        Instant now = clock.instant();

        if (currentToken.getUsedAt() != null) {
            revokeFamily(currentToken.getFamilyId(), now);

            return new RotationResult.Rejected(true);
        }

        if (!currentToken.isActiveAt(now)) {
            return reject();
        }

        return rotateActiveToken(currentToken, now);
    }

    private RotationResult.Success rotateActiveToken(
            RefreshToken currentToken,
            Instant now
    ) {
        currentToken.markAsUsed(now);

        String newRawToken = createToken(
                currentToken.getUser(),
                currentToken.getFamilyId(),
                now,
                currentToken.getExpiresAt()
        );

        return new RotationResult.Success(
                currentToken.getUser(),
                newRawToken
        );
    }

    private String createToken(
            User user,
            UUID familyId,
            Instant createdAt,
            Instant expiresAt
    ) {
        String rawToken = refreshTokenCodec.generate();
        String tokenHash = refreshTokenCodec.hash(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                user,
                familyId,
                tokenHash,
                createdAt,
                expiresAt
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private void revokeFamily(UUID familyId, Instant now) {
        refreshTokenRepository.findByFamilyId(familyId)
                .forEach(token -> token.revoke(now));
    }

    private RotationResult.Rejected reject() {
        return new RotationResult.Rejected(false);
    }
}