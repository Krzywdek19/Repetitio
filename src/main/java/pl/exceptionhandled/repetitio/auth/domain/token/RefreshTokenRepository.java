package pl.exceptionhandled.repetitio.auth.domain.token;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT rt
            FROM RefreshToken rt
            WHERE rt.tokenHash = :hash
            """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("hash") String hash
    );

    @Query("""
        SELECT rt.user.id
        FROM RefreshToken rt
        WHERE rt.tokenHash = :hash
        """)
    Optional<UUID> findUserIdByTokenHash(
            @Param("hash") String hash
    );

    List<RefreshToken> findByFamilyId(UUID familyId);
}