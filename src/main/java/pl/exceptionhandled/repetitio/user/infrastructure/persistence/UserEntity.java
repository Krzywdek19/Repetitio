package pl.exceptionhandled.repetitio.user.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class UserEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 254) private String email;
    @Column(nullable = false, length = 100) private String passwordHash;
    @Column(nullable = false) private Instant createdAt;
    UserEntity(UUID id, String email, String passwordHash, Instant createdAt) {
        this.id = id; this.email = email; this.passwordHash = passwordHash; this.createdAt = createdAt;
    }
}

