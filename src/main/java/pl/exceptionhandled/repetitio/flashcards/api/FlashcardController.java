package pl.exceptionhandled.repetitio.flashcards.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.repetitio.flashcards.application.*;
import pl.exceptionhandled.repetitio.shared.api.PageResponse;
import static pl.exceptionhandled.repetitio.shared.security.CurrentUser.id;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
class FlashcardController {
    private final FlashcardService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    FlashcardData create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateFlashcardRequest request) {
        return service.create(id(jwt), request.noteId(), request.front(), request.back());
    }
    @GetMapping
    PageResponse<FlashcardData> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(id(jwt), page, size);
    }
    @GetMapping("/{resourceId}")
    FlashcardData get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId) { return service.get(id(jwt), resourceId); }
    @PatchMapping("/{resourceId}")
    FlashcardData update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId,
                    @RequestBody UpdateFlashcardRequest request) {
        return service.update(id(jwt), resourceId, request.noteIdPresent(), request.getNoteId(), request.getFront(), request.getBack());
    }
    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId) { service.delete(id(jwt), resourceId); }
}

