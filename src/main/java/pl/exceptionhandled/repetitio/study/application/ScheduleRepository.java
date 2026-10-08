package pl.exceptionhandled.repetitio.study.application;
import java.util.*;
public interface ScheduleRepository {
    void insert(ScheduleData data);
    Optional<ScheduleData> findByFlashcardId(UUID flashcardId);
    ScheduleData update(ScheduleData data);
}

