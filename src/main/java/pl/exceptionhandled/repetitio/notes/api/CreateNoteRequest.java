package pl.exceptionhandled.repetitio.notes.api;
import jakarta.validation.constraints.*;

public record CreateNoteRequest(@NotBlank @Size(max = 200) String title,
                                @NotBlank @Size(max = 50000) String content) {}

