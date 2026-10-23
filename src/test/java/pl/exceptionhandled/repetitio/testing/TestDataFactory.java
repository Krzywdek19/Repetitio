package pl.exceptionhandled.repetitio.testing;

import java.time.Clock;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import pl.exceptionhandled.repetitio.auth.application.AuthService;
import pl.exceptionhandled.repetitio.notes.application.NoteData;
import pl.exceptionhandled.repetitio.notes.application.NoteRepository;

@RequiredArgsConstructor
public class TestDataFactory {

    private final AuthService auth;
    private final NoteRepository notes;
    private final Clock clock;

    public UUID createOwner() {
        return auth.register(UUID.randomUUID() + "@example.com",
                        "a-safe-test-password")
                .id();
    }

    public NoteData createNote(
            UUID ownerId,
            String title,
            String content
    ) {
        var now = clock.instant();

        var note = new NoteData(
                UUID.randomUUID(),
                ownerId,
                title,
                content,
                now,
                now
        );

        return notes.save(note);
    }
}