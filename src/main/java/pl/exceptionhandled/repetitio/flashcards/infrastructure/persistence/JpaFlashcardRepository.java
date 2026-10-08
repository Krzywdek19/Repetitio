package pl.exceptionhandled.repetitio.flashcards.infrastructure.persistence;
import java.util.*;
import org.springframework.data.jpa.repository.*;
interface JpaFlashcardRepository extends JpaRepository<FlashcardEntity, UUID> {
    Optional<FlashcardEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
    @Modifying
    @Query("delete from FlashcardEntity e where e.id = :id and e.ownerId = :ownerId")
    void deleteOwned(UUID id, UUID ownerId);
}

