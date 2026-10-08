package pl.exceptionhandled.repetitio.flashcards.application;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.repetitio.shared.api.PageResponse;
import pl.exceptionhandled.repetitio.shared.error.ApiException;
import pl.exceptionhandled.repetitio.notes.application.NoteService;
import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
@Transactional
public class FlashcardService {
    private final FlashcardRepository repository;
    private final Clock clock;
    private final NoteService notes;
    private final ApplicationEventPublisher events;

    public FlashcardData create(UUID owner, UUID noteId, String front, String back) {
        if (noteId != null) notes.get(owner, noteId);
        var now = clock.instant();
        var data = repository.save(new FlashcardData(UUID.randomUUID(), owner, noteId, front, back, now, now));
        events.publishEvent(new FlashcardCreated(data.id(), now));
        return data;
    }

    @Transactional(readOnly = true)
    public FlashcardData get(UUID owner, UUID id) { return repository.findOwned(id, owner).orElseThrow(ApiException::notFound); }

    @Transactional(readOnly = true)
    public PageResponse<FlashcardData> list(UUID owner, int page, int size) {
        return PageResponse.of(repository.listOwned(owner, Math.multiplyExact(page, size), size + 1), page, size);
    }

    public FlashcardData update(UUID owner, UUID id, boolean noteIdPresent, UUID noteId, String front, String back) {
        var old = get(owner, id);
        if (noteIdPresent && noteId != null) notes.get(owner, noteId);
        return repository.save(new FlashcardData(id, owner, noteIdPresent ? noteId : old.noteId(), 
                front == null ? old.front() : front,
                back == null ? old.back() : back, old.createdAt(), clock.instant()));
    }

    public void delete(UUID owner, UUID id) { get(owner, id); repository.deleteOwned(id, owner); }
}

