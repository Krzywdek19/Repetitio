package pl.exceptionhandled.repetitio.notes.application;
import java.time.Instant;
import java.util.UUID;
public record NoteData(UUID id, UUID ownerId, String title, String content,
                        Instant createdAt, Instant updatedAt) {}

