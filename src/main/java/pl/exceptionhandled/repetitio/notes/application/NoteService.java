package pl.exceptionhandled.repetitio.notes.application;

import java.time.Clock;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.exceptionhandled.repetitio.shared.api.PageResponse;
import pl.exceptionhandled.repetitio.shared.error.ApiException;


@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {
    private final NoteRepository repository;
    private final Clock clock;
    

    public NoteData create(UUID owner, String title, String content) {
        
        var now = clock.instant();
        var data = repository.save(new NoteData(UUID.randomUUID(), owner, title, content, now, now));
        
        return data;
    }

    @Transactional(readOnly = true)
    public NoteData get(UUID owner, UUID id) { return repository.findOwned(id, owner).orElseThrow(ApiException::notFound); }

    @Transactional(readOnly = true)
    public PageResponse<NoteData> list(UUID owner, int page, int size) {
        return PageResponse.of(repository.listOwned(owner, Math.multiplyExact(page, size), size + 1), page, size);
    }

    public NoteData update(UUID owner, UUID id, String title, String content) {
        var old = get(owner, id);
        
        return repository.save(new NoteData(id, owner, 
                title == null ? old.title() : title,
                content == null ? old.content() : content, old.createdAt(), clock.instant()));
    }

    public void delete(UUID owner, UUID id) { get(owner, id); repository.deleteOwned(id, owner); }
}

