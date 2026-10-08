package pl.exceptionhandled.repetitio.study.application;
import java.time.Instant;
import java.util.UUID;
public record DueFlashcard(UUID flashcardId, String front, String back, Instant nextReviewAt, long version) {}

