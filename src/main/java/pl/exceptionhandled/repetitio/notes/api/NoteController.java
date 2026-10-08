package pl.exceptionhandled.repetitio.notes.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.repetitio.notes.application.*;
import pl.exceptionhandled.repetitio.shared.api.PageResponse;
import static pl.exceptionhandled.repetitio.shared.security.CurrentUser.id;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
class NoteController {
    private final NoteService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    NoteData create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateNoteRequest request) {
        return service.create(id(jwt), request.title(), request.content());
    }
    @GetMapping
    PageResponse<NoteData> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(id(jwt), page, size);
    }
    @GetMapping("/{resourceId}")
    NoteData get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId) { return service.get(id(jwt), resourceId); }
    @PatchMapping("/{resourceId}")
    NoteData update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId,
                    @RequestBody UpdateNoteRequest request) {
        return service.update(id(jwt), resourceId, request.getTitle(), request.getContent());
    }
    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID resourceId) { service.delete(id(jwt), resourceId); }
}

