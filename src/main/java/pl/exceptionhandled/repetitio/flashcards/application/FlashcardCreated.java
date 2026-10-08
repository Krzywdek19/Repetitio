package pl.exceptionhandled.repetitio.flashcards.application;
import java.time.Instant;
import java.util.UUID;
public record FlashcardCreated(UUID flashcardId, Instant createdAt) {}

