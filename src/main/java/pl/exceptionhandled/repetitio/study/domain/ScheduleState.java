package pl.exceptionhandled.repetitio.study.domain;
import java.time.Instant;
public record ScheduleState(Instant nextReviewAt, Instant lastReviewedAt, int intervalDays,
                            int repetitionCount, double easeFactor) {}

