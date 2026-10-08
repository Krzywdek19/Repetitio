package pl.exceptionhandled.repetitio.user.application;
import java.util.UUID;
public record AuthenticationCredentials(UUID id, String passwordHash) {
    @Override public String toString() { return "AuthenticationCredentials[redacted]"; }
}

