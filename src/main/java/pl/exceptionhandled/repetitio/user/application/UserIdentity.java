package pl.exceptionhandled.repetitio.user.application;
import java.time.Instant;
import java.util.UUID;
public record UserIdentity(UUID id, String email, Instant createdAt) {}

