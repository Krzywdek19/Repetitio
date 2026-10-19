package pl.exceptionhandled.repetitio;

import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.exceptionhandled.repetitio.auth.application.AuthService;
import pl.exceptionhandled.repetitio.user.application.UserRepository;
import pl.exceptionhandled.repetitio.flashcards.application.FlashcardService;
import pl.exceptionhandled.repetitio.study.application.*;
import pl.exceptionhandled.repetitio.study.domain.Rating;
import pl.exceptionhandled.repetitio.shared.error.ApiException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConcurrencyIT extends PostgresIntegrationTest {
    @Autowired AuthService auth;
    @Autowired FlashcardService flashcards;
    @Autowired StudyService study;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean UserRepository users;
    @MockitoSpyBean ScheduleRepository schedules;

    @Test void concurrentRegistrationHasExactlyOneWinnerAfterBothExistenceChecksPass() throws Exception {
        var email = UUID.randomUUID() + "@example.com";
        var barrier = new CyclicBarrier(2);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            assertThat(result).isEqualTo(false);
            barrier.await(15, TimeUnit.SECONDS);
            return result;
        }).when(users).existsByEmail(email);
        var outcomes = concurrently(() -> auth.register(email, "concurrent-test-password"));
        assertThat(outcomes.stream().filter(Objects::isNull).count()).isEqualTo(1);
        var failure = outcomes.stream().filter(Objects::nonNull).findFirst().orElseThrow();
        assertThat(failure).isInstanceOf(ApiException.class);
        assertThat(((ApiException) failure).status()).isEqualTo(409);
        assertThat(jdbc.queryForObject("select count(*) from app_user where email = ?", Long.class, email)).isEqualTo(1);
        verify(users, times(2)).existsByEmail(email);
    }

    @Test void concurrentReviewsWithSameVersionHaveExactlyOneWinner() throws Exception {
        var owner = auth.register(UUID.randomUUID() + "@example.com", "concurrent-test-password").id();
        var card = flashcards.create(owner, null, "Question", "Answer");
        var barrier = new CyclicBarrier(2);
        doAnswer(invocation -> {
            Object result = invocation.callRealMethod();
            barrier.await(15, TimeUnit.SECONDS);
            return result;
        }).when(schedules).findByFlashcardId(card.id());
        var outcomes = concurrently(() -> study.review(owner, card.id(), Rating.GOOD, 0));
        assertThat(outcomes.stream().filter(Objects::isNull).count()).isEqualTo(1);
        assertThat(outcomes.stream().filter(Objects::nonNull).findFirst().orElseThrow())
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(jdbc.queryForObject("select version from flashcard_schedule where flashcard_id = ?", Long.class, card.id())).isEqualTo(1);
        assertThat(jdbc.queryForObject("select repetition_count from flashcard_schedule where flashcard_id = ?", Integer.class, card.id())).isEqualTo(1);
    }

    @Test void scheduleFailureRollsBackCardCreation() {
        var owner = auth.register(UUID.randomUUID() + "@example.com", "concurrent-test-password").id();
        doThrow(new IllegalStateException("Simulated schedule failure")).when(schedules).insert(any());
        assertThatThrownBy(() -> flashcards.create(owner, null, "Question", "Answer")).rootCause().isInstanceOf(IllegalStateException.class);
        assertThat(jdbc.queryForObject("select count(*) from flashcard where owner_id = ?", Long.class, owner)).isZero();
    }

    private List<Throwable> concurrently(Callable<?> action) throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Throwable> attempt = () -> {
                try { action.call(); return null; }
                catch (Throwable failure) { return failure; }
            };
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            return Arrays.asList(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS));
        }
    }
}

