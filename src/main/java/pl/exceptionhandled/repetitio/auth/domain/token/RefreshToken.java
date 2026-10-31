package pl.exceptionhandled.repetitio.auth.domain.token;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.exceptionhandled.repetitio.users.domain.User;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public RefreshToken(
            User user,
            UUID familyId,
            String tokenHash,
            Instant createdAt,
            Instant expiresAt
    ) {
        this.user = Objects.requireNonNull(user);
        this.familyId = Objects.requireNonNull(familyId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);

        if (!expiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException(
                    "Expiration must be after creation"
            );
        }
    }

    public boolean isExpiredAt(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean isActiveAt(Instant now) {
        return usedAt == null
                && revokedAt == null
                && !isExpiredAt(now);
    }

    public void markAsUsed(Instant now) {
        if (!isActiveAt(now)) {
            throw new IllegalStateException(
                    "Refresh token is no longer active"
            );
        }

        this.usedAt = now;
    }

    public void revoke(Instant now) {
        if (revokedAt == null) {
            this.revokedAt = Objects.requireNonNull(now);
        }
    }
}