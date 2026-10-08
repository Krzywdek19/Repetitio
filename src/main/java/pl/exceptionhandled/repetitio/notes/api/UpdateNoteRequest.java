package pl.exceptionhandled.repetitio.notes.api;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import pl.exceptionhandled.repetitio.shared.error.ApiException;


@Getter
public class UpdateNoteRequest {
    private String title;
    private String content;
    
    @JsonSetter
    public void setTitle(String value) { title = text(value, 200); }
    @JsonSetter
    public void setContent(String value) { content = text(value, 50000); }
    
    private static String text(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max) throw new ApiException(400, "Invalid text field");
        return value;
    }
}

