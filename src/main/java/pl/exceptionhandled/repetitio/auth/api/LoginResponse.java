package pl.exceptionhandled.repetitio.auth.api;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {}