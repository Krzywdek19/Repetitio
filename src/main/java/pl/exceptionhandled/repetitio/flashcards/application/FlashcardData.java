package pl.exceptionhandled.repetitio.flashcards.application;
import java.time.Instant;
import java.util.UUID;
public record FlashcardData(UUID id, UUID ownerId, UUID noteId, String front, String back,
                        Instant createdAt, Instant updatedAt) {}

