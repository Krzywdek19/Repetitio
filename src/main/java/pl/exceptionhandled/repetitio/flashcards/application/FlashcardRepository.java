package pl.exceptionhandled.repetitio.flashcards.application;
import java.util.*;
public interface FlashcardRepository {
    FlashcardData save(FlashcardData data);
    Optional<FlashcardData> findOwned(UUID id, UUID ownerId);
    List<FlashcardData> listOwned(UUID ownerId, int offset, int limit);
    void deleteOwned(UUID id, UUID ownerId);
}

