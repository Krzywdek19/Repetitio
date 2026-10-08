package pl.exceptionhandled.repetitio.flashcards.infrastructure.persistence;
import java.util.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.exceptionhandled.repetitio.flashcards.application.*;

@Repository
@RequiredArgsConstructor
class FlashcardPersistenceAdapter implements FlashcardRepository {
    private final JpaFlashcardRepository repository;
    private final EntityManager entityManager;
    public FlashcardData save(FlashcardData data) { return repository.saveAndFlush(new FlashcardEntity(data)).data(); }
    public Optional<FlashcardData> findOwned(UUID id, UUID ownerId) { return repository.findByIdAndOwnerId(id, ownerId).map(FlashcardEntity::data); }
    public List<FlashcardData> listOwned(UUID ownerId, int offset, int limit) {
        return entityManager.createQuery("select e from FlashcardEntity e where e.ownerId = :owner order by e.createdAt desc, e.id", FlashcardEntity.class)
                .setParameter("owner", ownerId).setFirstResult(offset).setMaxResults(limit).getResultList()
                .stream().map(FlashcardEntity::data).toList();
    }
    public void deleteOwned(UUID id, UUID ownerId) { repository.deleteOwned(id, ownerId); }
}

