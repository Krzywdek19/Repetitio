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

    @Override
    public List<NoteData> searchOwned(
            UUID ownerId,
            String query,
            int offset,
            int limit
    ) {
        var escaped = query
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        var pattern = "%" + escaped.toLowerCase(Locale.ROOT) + "%";

        return entityManager.createQuery("""
            SELECT n
            FROM NoteEntity n
            WHERE n.ownerId = :ownerId
              AND (
                  LOWER(n.title) LIKE :pattern ESCAPE '!'
                  OR LOWER(n.content) LIKE :pattern ESCAPE '!'
              )
            ORDER BY n.createdAt DESC, n.id
            """, NoteEntity.class)
                .setParameter("ownerId", ownerId)
                .setParameter("pattern", pattern)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(NoteEntity::data)
                .toList();
    }

    public void deleteOwned(UUID id, UUID ownerId) { repository.deleteOwned(id, ownerId); }
}

