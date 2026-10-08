package pl.exceptionhandled.repetitio.study.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import pl.exceptionhandled.repetitio.study.application.ScheduleData;
import pl.exceptionhandled.repetitio.study.domain.ScheduleState;

@Entity
@Table(name = "flashcard_schedule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class FlashcardScheduleEntity {
    @Id private UUID id;
    @Column(nullable = false, updatable = false) private UUID flashcardId;
    @Column(nullable = false) private Instant nextReviewAt;
    private Instant lastReviewedAt;
    @Column(nullable = false) private int intervalDays;
    @Column(nullable = false) private int repetitionCount;
    @Column(nullable = false) private double easeFactor;
    @Version private Long version;

    FlashcardScheduleEntity(ScheduleData data) {
        id = data.id(); flashcardId = data.flashcardId();
        apply(data.state());
    }
    void apply(ScheduleState state) {
        nextReviewAt = state.nextReviewAt(); lastReviewedAt = state.lastReviewedAt();
        intervalDays = state.intervalDays(); repetitionCount = state.repetitionCount(); easeFactor = state.easeFactor();
    }
    ScheduleData data() {
        return new ScheduleData(id, flashcardId, new ScheduleState(nextReviewAt, lastReviewedAt, intervalDays,
                repetitionCount, easeFactor), version);
    }
}

