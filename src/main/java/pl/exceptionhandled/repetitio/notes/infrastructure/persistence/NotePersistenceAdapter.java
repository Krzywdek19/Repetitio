package pl.exceptionhandled.repetitio.notes.infrastructure.persistence;
import java.util.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.exceptionhandled.repetitio.notes.application.*;

@Repository
@RequiredArgsConstructor
class NotePersistenceAdapter implements NoteRepository {
    private final JpaNoteRepository repository;
    private final EntityManager entityManager;
    public NoteData save(NoteData data) { return repository.saveAndFlush(new NoteEntity(data)).data(); }
    public Optional<NoteData> findOwned(UUID id, UUID ownerId) { return repository.findByIdAndOwnerId(id, ownerId).map(NoteEntity::data); }
    public List<NoteData> listOwned(UUID ownerId, int offset, int limit) {
        return entityManager.createQuery("select e from NoteEntity e where e.ownerId = :owner order by e.createdAt desc, e.id", NoteEntity.class)
                .setParameter("owner", ownerId).setFirstResult(offset).setMaxResults(limit).getResultList()
                .stream().map(NoteEntity::data).toList();
    }
    public void deleteOwned(UUID id, UUID ownerId) { repository.deleteOwned(id, ownerId); }
}

