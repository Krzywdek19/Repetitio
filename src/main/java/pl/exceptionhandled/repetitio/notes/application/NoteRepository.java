package pl.exceptionhandled.repetitio.notes.application;
import java.util.*;
public interface NoteRepository {
    NoteData save(NoteData data);
    Optional<NoteData> findOwned(UUID id, UUID ownerId);
    List<NoteData> listOwned(UUID ownerId, int offset, int limit);
    void deleteOwned(UUID id, UUID ownerId);
}

