package pl.exceptionhandled.repetitio.study.infrastructure.persistence;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
interface JpaScheduleRepository extends JpaRepository<FlashcardScheduleEntity, UUID> {
    Optional<FlashcardScheduleEntity> findByFlashcardId(UUID flashcardId);
}

