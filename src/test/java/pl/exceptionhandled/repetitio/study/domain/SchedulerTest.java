package pl.exceptionhandled.repetitio.study.domain;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.*;

class SchedulerTest {
    private final Instant now = Instant.parse("2026-10-07T12:00:00Z");

    @Test void newCardsAreImmediatelyDue() {
        var state = Scheduler.initial(now);
        assertThat(state.nextReviewAt()).isEqualTo(now);
        assertThat(state.lastReviewedAt()).isNull();
        assertThat(state.repetitionCount()).isZero();
    }

    @ParameterizedTest
    @CsvSource({"AGAIN,1,0", "HARD,1,1", "GOOD,2,1", "EASY,4,1"})
    void firstReview(Rating rating, int days, int repetitions) {
        var state = Scheduler.review(Scheduler.initial(now), rating, now);
        assertThat(state.intervalDays()).isEqualTo(days);
        assertThat(state.repetitionCount()).isEqualTo(repetitions);
        assertThat(state.nextReviewAt()).isEqualTo(now.plusSeconds(days * 86400L));
        assertThat(state.lastReviewedAt()).isEqualTo(now);
    }

    @Test void forgettingResetsProgressAndEaseHasALowerBound() {
        var old = new ScheduleState(now, now.minusSeconds(86400), 100, 10, 1.3);
        var state = Scheduler.review(old, Rating.AGAIN, now);
        assertThat(state.repetitionCount()).isZero();
        assertThat(state.intervalDays()).isEqualTo(1);
        assertThat(state.easeFactor()).isEqualTo(1.3);
    }

    @Test void progressHasUpperBounds() {
        var old = new ScheduleState(now, now, 3650, 1000000, 3.0);
        var state = Scheduler.review(old, Rating.EASY, now);
        assertThat(state.intervalDays()).isEqualTo(3650);
        assertThat(state.easeFactor()).isEqualTo(3.0);
        assertThat(state.repetitionCount()).isEqualTo(1000000);
    }

    @ParameterizedTest
    @CsvSource({"HARD,12", "GOOD,25", "EASY,34"})
    void subsequentReviewsGrowInterval(Rating rating, int days) {
        var old = new ScheduleState(now, now, 10, 3, 2.5);
        assertThat(Scheduler.review(old, rating, now).intervalDays()).isEqualTo(days);
    }
}

