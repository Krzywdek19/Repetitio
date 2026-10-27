package pl.exceptionhandled.repetitio.auth.api;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String email
) {
}
