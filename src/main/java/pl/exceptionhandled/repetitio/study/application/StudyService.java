package pl.exceptionhandled.repetitio.study.application;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import pl.exceptionhandled.repetitio.flashcards.application.*;
import pl.exceptionhandled.repetitio.shared.api.PageResponse;
import pl.exceptionhandled.repetitio.shared.error.ApiException;
import pl.exceptionhandled.repetitio.study.domain.*;

@Service
@RequiredArgsConstructor
public class StudyService {
    private final ScheduleRepository schedules;
    private final DueFlashcardQuery dueCards;
    private final FlashcardService flashcards;
    private final Clock clock;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void initialize(FlashcardCreated event) {
        schedules.insert(new ScheduleData(UUID.randomUUID(), event.flashcardId(), Scheduler.initial(event.createdAt()), 0));
    }

    @Transactional(readOnly = true)
    public PageResponse<DueFlashcard> due(UUID ownerId, int page, int size) {
        return PageResponse.of(dueCards.findDue(ownerId, clock.instant(), Math.multiplyExact(page, size), size + 1), page, size);
    }

    @Transactional
    public ScheduleData review(UUID ownerId, UUID flashcardId, Rating rating, long expectedVersion) {
        flashcards.get(ownerId, flashcardId);
        var schedule = schedules.findByFlashcardId(flashcardId).orElseThrow(ApiException::notFound);
        if (schedule.version() != expectedVersion) throw ApiException.conflict();
        var now = clock.instant();
        if (schedule.state().nextReviewAt().isAfter(now))
            throw new ApiException(409, "Flashcard is not due for review");
        var next = Scheduler.review(schedule.state(), rating, now);
        return schedules.update(new ScheduleData(schedule.id(), flashcardId, next, schedule.version()));
    }
}

