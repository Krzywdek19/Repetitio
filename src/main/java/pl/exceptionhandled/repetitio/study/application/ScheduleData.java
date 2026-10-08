package pl.exceptionhandled.repetitio.study.application;
import java.util.UUID;
import pl.exceptionhandled.repetitio.study.domain.ScheduleState;
public record ScheduleData(UUID id, UUID flashcardId, ScheduleState state, long version) {}

