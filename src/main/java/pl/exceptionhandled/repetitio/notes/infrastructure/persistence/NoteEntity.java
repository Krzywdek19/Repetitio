package pl.exceptionhandled.repetitio.notes.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import pl.exceptionhandled.repetitio.notes.application.NoteData;

@Entity
@Table(name = "note")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class NoteEntity {
    @Id private UUID id;
    @Column(nullable = false, updatable = false) private UUID ownerId;
    
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, length = 50000) private String content;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    NoteEntity(NoteData data) {
        id = data.id(); ownerId = data.ownerId(); 
        title = data.title(); content = data.content();
        createdAt = data.createdAt(); updatedAt = data.updatedAt();
    }
    NoteData data() { return new NoteData(id, ownerId, title, content, createdAt, updatedAt); }
}

