package pl.exceptionhandled.repetitio.auth.api;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}