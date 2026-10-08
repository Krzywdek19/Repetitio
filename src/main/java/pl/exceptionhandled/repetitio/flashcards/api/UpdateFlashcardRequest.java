package pl.exceptionhandled.repetitio.flashcards.api;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import pl.exceptionhandled.repetitio.shared.error.ApiException;
import java.util.UUID;

@Getter
public class UpdateFlashcardRequest {
    private String front;
    private String back;
    private UUID noteId;
    @Getter(lombok.AccessLevel.NONE)
    private boolean noteIdPresent;
    public boolean noteIdPresent() { return noteIdPresent; }
    @JsonSetter
    public void setFront(String value) { front = text(value, 5000); }
    @JsonSetter
    public void setBack(String value) { back = text(value, 5000); }
    @JsonSetter
    public void setNoteId(UUID value) { noteId = value; noteIdPresent = true; }
    private static String text(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max) throw new ApiException(400, "Invalid text field");
        return value;
    }
}

