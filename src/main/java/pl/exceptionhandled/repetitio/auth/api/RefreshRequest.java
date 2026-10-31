package pl.exceptionhandled.repetitio.auth.api;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken
) {}