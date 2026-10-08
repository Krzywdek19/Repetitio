package pl.exceptionhandled.repetitio;

import java.time.Instant;
import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.exceptionhandled.repetitio.auth.application.AuthService;
import pl.exceptionhandled.repetitio.flashcards.application.FlashcardService;
import pl.exceptionhandled.repetitio.study.application.DueFlashcardQuery;
import static org.assertj.core.api.Assertions.*;

class DueQueryIT extends PostgresIntegrationTest {
    @Autowired AuthService auth;
    @Autowired FlashcardService flashcards;
    @Autowired DueFlashcardQuery query;
    @Autowired JdbcTemplate jdbc;

    @Test void includesExactDueTimeAndPaginatesAfterOwnerFiltering() {
        var owner = auth.register(UUID.randomUUID() + "@example.com", "query-test-password").id();
        var stranger = auth.register(UUID.randomUUID() + "@example.com", "query-test-password").id();
        var a = flashcards.create(owner, null, "a", "a");
        var b = flashcards.create(owner, null, "b", "b");
        var c = flashcards.create(owner, null, "c", "c");
        flashcards.create(stranger, null, "private", "private");
        var now = Instant.parse("2030-01-01T00:00:00Z");
        jdbc.update("update flashcard_schedule set next_review_at = ? where flashcard_id in (?, ?)", Timestamp.from(now), a.id(), b.id());
        jdbc.update("update flashcard_schedule set next_review_at = ? where flashcard_id = ?", Timestamp.from(now.plusSeconds(1)), c.id());
        var first = query.findDue(owner, now, 0, 1);
        var second = query.findDue(owner, now, 1, 1);
        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        assertThat(first.getFirst().flashcardId()).isNotEqualTo(second.getFirst().flashcardId());
        assertThat(query.findDue(owner, now, 0, 10)).extracting(row -> row.flashcardId()).containsExactlyInAnyOrder(a.id(), b.id());
    }
}

