package pl.exceptionhandled.repetitio.study.application;
import java.time.Instant;
import java.util.*;
public interface DueFlashcardQuery {
    List<DueFlashcard> findDue(UUID ownerId, Instant now, int offset, int limit);
}

