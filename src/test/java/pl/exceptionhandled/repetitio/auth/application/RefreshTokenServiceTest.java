package pl.exceptionhandled.repetitio.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.exceptionhandled.repetitio.auth.domain.token.RefreshToken;
import pl.exceptionhandled.repetitio.auth.domain.token.RefreshTokenRepository;
import pl.exceptionhandled.repetitio.auth.infrastructure.security.RefreshTokenCodec;
import pl.exceptionhandled.repetitio.users.domain.User;
import pl.exceptionhandled.repetitio.users.domain.UserRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant FIXED_INSTANT =
            Instant.parse("2026-10-30T12:00:00Z");

    private static final Instant TOKEN_CREATED_AT =
            FIXED_INSTANT.minus(Duration.ofDays(1));

    private static final Instant TOKEN_EXPIRES_AT =
            FIXED_INSTANT.plus(Duration.ofDays(13));

    private static final UUID USER_ID =
            UUID.randomUUID();

    private static final UUID FAMILY_ID =
            UUID.randomUUID();

    private static final String RAW_TOKEN =
            "example-raw-refresh-token";

    private static final String TOKEN_HASH =
            "a".repeat(64);

    private static final String NEW_RAW_TOKEN =
            "example-new-refresh-token";

    private static final String NEW_TOKEN_HASH =
            "b".repeat(64);

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenCodec refreshTokenCodec;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<RefreshToken> tokenCaptor;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                FIXED_INSTANT,
                ZoneOffset.UTC
        );

        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                refreshTokenCodec,
                userRepository,
                clock
        );

        user = new User(
                "test@example.com",
                "hashed-password"
        );
    }

    @Test
    void shouldIssueRefreshToken() {
        stubTokenGeneration();

        String result = refreshTokenService.issue(user);

        assertEquals(NEW_RAW_TOKEN, result);

        verify(refreshTokenRepository)
                .save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();

        assertSame(user, savedToken.getUser());
        assertNotNull(savedToken.getFamilyId());

        verify(refreshTokenCodec).generate();
        verify(refreshTokenCodec).hash(NEW_RAW_TOKEN);
    }

    @Test
    void shouldStoreOnlyHashedToken() {
        stubTokenGeneration();

        refreshTokenService.issue(user);

        verify(refreshTokenRepository)
                .save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();

        assertEquals(
                NEW_TOKEN_HASH,
                savedToken.getTokenHash()
        );

        assertNotEquals(
                NEW_RAW_TOKEN,
                savedToken.getTokenHash()
        );

        assertEquals(
                64,
                savedToken.getTokenHash().length()
        );
    }

    @Test
    void shouldSetCorrectExpirationDate() {
        stubTokenGeneration();

        refreshTokenService.issue(user);

        verify(refreshTokenRepository)
                .save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();

        assertEquals(
                FIXED_INSTANT,
                savedToken.getCreatedAt()
        );

        assertEquals(
                FIXED_INSTANT.plus(Duration.ofDays(14)),
                savedToken.getExpiresAt()
        );
    }

    @Test
    void shouldRotateValidToken() {
        RefreshToken currentToken = createActiveToken();

        stubTokenLookup(currentToken);
        stubTokenGeneration();

        RotationResult result = refreshTokenService.rotate(RAW_TOKEN);

        RotationResult.Success success =
                assertInstanceOf(RotationResult.Success.class, result);

        assertSame(user, success.user());
        assertEquals(NEW_RAW_TOKEN, success.refreshToken());

        assertEquals(
                FIXED_INSTANT,
                currentToken.getUsedAt()
        );

        verify(refreshTokenRepository)
                .save(tokenCaptor.capture());

        RefreshToken newToken = tokenCaptor.getValue();

        assertSame(user, newToken.getUser());

        assertEquals(
                FAMILY_ID,
                newToken.getFamilyId()
        );

        assertEquals(
                TOKEN_EXPIRES_AT,
                newToken.getExpiresAt()
        );

        assertEquals(
                NEW_TOKEN_HASH,
                newToken.getTokenHash()
        );

        assertNull(newToken.getUsedAt());
        assertNull(newToken.getRevokedAt());
    }

    @Test
    void shouldRejectUnknownToken() {
        when(refreshTokenCodec.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(refreshTokenRepository.findUserIdByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.empty());

        RotationResult result = refreshTokenService.rotate(RAW_TOKEN);

        RotationResult.Rejected rejected =
                assertInstanceOf(RotationResult.Rejected.class, result);

        assertFalse(rejected.reuseDetected());

        verify(refreshTokenRepository, never())
                .save(any());

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldRejectExpiredToken() {
        RefreshToken expiredToken = new RefreshToken(
                user,
                FAMILY_ID,
                TOKEN_HASH,
                FIXED_INSTANT.minus(Duration.ofDays(15)),
                FIXED_INSTANT.minus(Duration.ofDays(1))
        );

        stubTokenLookup(expiredToken);

        RotationResult result = refreshTokenService.rotate(RAW_TOKEN);

        RotationResult.Rejected rejected =
                assertInstanceOf(RotationResult.Rejected.class, result);

        assertFalse(rejected.reuseDetected());

        assertNull(expiredToken.getUsedAt());

        verify(refreshTokenRepository, never())
                .save(any());
    }

    @Test
    void shouldRejectRevokedToken() {
        RefreshToken revokedToken = createActiveToken();

        revokedToken.revoke(
                FIXED_INSTANT.minusSeconds(60)
        );

        stubTokenLookup(revokedToken);

        RotationResult result = refreshTokenService.rotate(RAW_TOKEN);

        RotationResult.Rejected rejected =
                assertInstanceOf(RotationResult.Rejected.class, result);

        assertFalse(rejected.reuseDetected());

        verify(refreshTokenRepository, never())
                .save(any());
    }

    @Test
    void shouldRevokeFamilyOnTokenReuse() {
        RefreshToken usedToken = createActiveToken();

        usedToken.markAsUsed(
                FIXED_INSTANT.minusSeconds(60)
        );

        RefreshToken activeToken = new RefreshToken(
                user,
                FAMILY_ID,
                NEW_TOKEN_HASH,
                FIXED_INSTANT.minusSeconds(30),
                TOKEN_EXPIRES_AT
        );

        stubTokenLookup(usedToken);

        when(refreshTokenRepository.findByFamilyId(FAMILY_ID))
                .thenReturn(List.of(usedToken, activeToken));

        RotationResult result = refreshTokenService.rotate(RAW_TOKEN);

        RotationResult.Rejected rejected =
                assertInstanceOf(RotationResult.Rejected.class, result);

        assertTrue(rejected.reuseDetected());

        assertEquals(
                FIXED_INSTANT,
                usedToken.getRevokedAt()
        );

        assertEquals(
                FIXED_INSTANT,
                activeToken.getRevokedAt()
        );

        verify(refreshTokenRepository, never())
                .save(any());

        verify(refreshTokenRepository)
                .findByFamilyId(FAMILY_ID);
    }

    @Test
    void shouldRejectBlankToken() {
        RotationResult result = refreshTokenService.rotate(" ");

        RotationResult.Rejected rejected =
                assertInstanceOf(RotationResult.Rejected.class, result);

        assertFalse(rejected.reuseDetected());

        verifyNoInteractions(
                refreshTokenRepository,
                refreshTokenCodec,
                userRepository
        );
    }


    private RefreshToken createActiveToken() {
        return new RefreshToken(
                user,
                FAMILY_ID,
                TOKEN_HASH,
                TOKEN_CREATED_AT,
                TOKEN_EXPIRES_AT
        );
    }

    private void stubTokenGeneration() {
        when(refreshTokenCodec.generate())
                .thenReturn(NEW_RAW_TOKEN);

        when(refreshTokenCodec.hash(NEW_RAW_TOKEN))
                .thenReturn(NEW_TOKEN_HASH);
    }

    private void stubTokenLookup(RefreshToken token) {
        when(refreshTokenCodec.hash(RAW_TOKEN))
                .thenReturn(TOKEN_HASH);

        when(refreshTokenRepository.findUserIdByTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(USER_ID));

        when(userRepository.findByIdForUpdate(USER_ID))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.findByTokenHashForUpdate(TOKEN_HASH))
                .thenReturn(Optional.of(token));
    }
}