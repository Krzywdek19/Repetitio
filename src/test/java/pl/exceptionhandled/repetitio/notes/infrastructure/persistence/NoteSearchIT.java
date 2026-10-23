package pl.exceptionhandled.repetitio.notes.infrastructure.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.exceptionhandled.repetitio.PostgresIntegrationTest;
import pl.exceptionhandled.repetitio.auth.application.AuthService;
import pl.exceptionhandled.repetitio.notes.application.NoteData;
import pl.exceptionhandled.repetitio.notes.application.NoteRepository;
import pl.exceptionhandled.repetitio.testing.TestDataFactory;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class NoteSearchIT extends PostgresIntegrationTest {

    @Autowired
    NoteRepository noteRepository;

    @Autowired
    AuthService auth;

    @Autowired
    Clock clock;

    private TestDataFactory testData;

    @BeforeEach
    void setUp() {
        testData = new TestDataFactory(auth, noteRepository, clock);
    }

    @Test
    void shouldSearchLiteralSpecialCharactersInContent() {
        var owner = testData.createOwner();

        var matchingNote = testData.createNote(
                owner,
                "something",
                "something % something"
        );

        testData.createNote(owner, "something", "something 1 something");

        var results = noteRepository.searchOwned(owner, "%", 0, 10);

        assertThat(results)
                .extracting(NoteData::id)
                .containsExactly(matchingNote.id());
    }

}
