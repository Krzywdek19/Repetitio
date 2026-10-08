package pl.exceptionhandled.repetitio.study.infrastructure.persistence;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.exceptionhandled.repetitio.study.application.*;
import pl.exceptionhandled.repetitio.shared.error.ApiException;

@Repository
@RequiredArgsConstructor
class SchedulePersistenceAdapter implements ScheduleRepository {
    private final JpaScheduleRepository repository;
    public void insert(ScheduleData data) { repository.saveAndFlush(new FlashcardScheduleEntity(data)); }
    public Optional<ScheduleData> findByFlashcardId(UUID flashcardId) {
        return repository.findByFlashcardId(flashcardId).map(FlashcardScheduleEntity::data);
    }
    public ScheduleData update(ScheduleData data) {
        var entity = repository.findById(data.id()).orElseThrow(ApiException::notFound);
        if (entity.getVersion() != data.version()) throw ApiException.conflict();
        entity.apply(data.state());
        repository.flush();
        return entity.data();
    }
}

