package pl.exceptionhandled.repetitio.study.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class Scheduler {
    public static final int MAX_INTERVAL_DAYS = 3650;
    private Scheduler() {}

    public static ScheduleState initial(Instant now) {
        return new ScheduleState(now, null, 0, 0, 2.5);
    }

    public static ScheduleState review(ScheduleState old, Rating rating, Instant now) {
        int repetitions = rating == Rating.AGAIN ? 0 : Math.min(old.repetitionCount() + 1, 1000000);
        double ease = Math.clamp(old.easeFactor() + switch (rating) {
            case AGAIN -> -0.2;
            case HARD -> -0.15;
            case GOOD -> 0;
            case EASY -> 0.15;
        }, 1.3, 3.0);
        long interval;
        if (rating == Rating.AGAIN) interval = 1;
        else if (old.repetitionCount() == 0) interval = switch (rating) {
            case HARD -> 1;
            case GOOD -> 2;
            case EASY -> 4;
            default -> throw new IllegalStateException();
        };
        else {
            double factor = switch (rating) {
                case HARD -> 1.2;
                case GOOD -> ease;
                case EASY -> ease * 1.3;
                default -> throw new IllegalStateException();
            };
            interval = Math.max((long) old.intervalDays() + 1, Math.round(old.intervalDays() * factor));
        }
        int days = (int) Math.clamp(interval, 1, MAX_INTERVAL_DAYS);
        return new ScheduleState(now.plus(days, ChronoUnit.DAYS), now, days, repetitions, ease);
    }
}

