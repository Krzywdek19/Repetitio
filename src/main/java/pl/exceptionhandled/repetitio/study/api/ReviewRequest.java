package pl.exceptionhandled.repetitio.study.api;
import jakarta.validation.constraints.*;
import pl.exceptionhandled.repetitio.study.domain.Rating;
public record ReviewRequest(@NotNull Rating rating, @NotNull @Min(0) Long expectedVersion) {}

