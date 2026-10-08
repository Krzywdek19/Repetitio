package pl.exceptionhandled.repetitio.study.api;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.repetitio.shared.api.PageResponse;
import pl.exceptionhandled.repetitio.study.application.*;
import static pl.exceptionhandled.repetitio.shared.security.CurrentUser.id;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
class StudyController {
    private final StudyService study;
    @GetMapping("/due")
    PageResponse<DueFlashcard> due(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") @Min(0) @Max(100000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return study.due(id(jwt), page, size);
    }
    @PostMapping("/flashcards/{flashcardId}/review")
    ScheduleData review(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID flashcardId,
                        @Valid @RequestBody ReviewRequest request) {
        return study.review(id(jwt), flashcardId, request.rating(), request.expectedVersion());
    }
}

