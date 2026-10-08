package pl.exceptionhandled.repetitio.notes.infrastructure.persistence;
import java.util.*;
import org.springframework.data.jpa.repository.*;
interface JpaNoteRepository extends JpaRepository<NoteEntity, UUID> {
    Optional<NoteEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
    @Modifying
    @Query("delete from NoteEntity e where e.id = :id and e.ownerId = :ownerId")
    void deleteOwned(UUID id, UUID ownerId);
}

