package pl.exceptionhandled.repetitio.user.infrastructure.persistence;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
}

