package pl.exceptionhandled.repetitio.auth.application;
public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    @Override public String toString() { return "TokenResponse[redacted]"; }
}

