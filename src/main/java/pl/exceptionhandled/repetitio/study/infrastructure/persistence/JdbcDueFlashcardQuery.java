package pl.exceptionhandled.repetitio.study.infrastructure.persistence;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.exceptionhandled.repetitio.study.application.*;

@Repository
@RequiredArgsConstructor
class JdbcDueFlashcardQuery implements DueFlashcardQuery {
    private final JdbcTemplate jdbc;
    public List<DueFlashcard> findDue(UUID ownerId, Instant now, int offset, int limit) {
        return jdbc.query("""
                select f.id, f.front, f.back, s.next_review_at, s.version
                from flashcard_schedule s join flashcard f on f.id = s.flashcard_id
                where f.owner_id = ? and s.next_review_at <= ?
                order by s.next_review_at, f.id limit ? offset ?
                """, (row, index) -> new DueFlashcard(row.getObject("id", UUID.class), row.getString("front"),
                row.getString("back"), row.getTimestamp("next_review_at").toInstant(), row.getLong("version")),
                ownerId, Timestamp.from(now), limit, offset);
    }
}

