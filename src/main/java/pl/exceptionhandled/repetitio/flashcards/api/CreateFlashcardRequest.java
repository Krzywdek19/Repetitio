package pl.exceptionhandled.repetitio.flashcards.api;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record CreateFlashcardRequest(UUID noteId, @NotBlank @Size(max = 5000) String front,
                                @NotBlank @Size(max = 5000) String back) {}

