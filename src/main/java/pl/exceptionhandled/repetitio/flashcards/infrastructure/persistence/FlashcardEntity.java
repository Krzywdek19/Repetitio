package pl.exceptionhandled.repetitio.flashcards.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import pl.exceptionhandled.repetitio.flashcards.application.FlashcardData;

@Entity
@Table(name = "flashcard")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class FlashcardEntity {
    @Id private UUID id;
    @Column(nullable = false, updatable = false) private UUID ownerId;
    private UUID noteId;
    @Column(nullable = false, length = 5000) private String front;
    @Column(nullable = false, length = 5000) private String back;
    @Column(nullable = false, updatable = false) private Instant createdAt;
    @Column(nullable = false) private Instant updatedAt;
    FlashcardEntity(FlashcardData data) {
        id = data.id(); ownerId = data.ownerId(); noteId = data.noteId();
        front = data.front(); back = data.back();
        createdAt = data.createdAt(); updatedAt = data.updatedAt();
    }
    FlashcardData data() { return new FlashcardData(id, ownerId, noteId, front, back, createdAt, updatedAt); }
}

