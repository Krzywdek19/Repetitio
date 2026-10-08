package pl.exceptionhandled.repetitio.auth.api;

import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;

public record CredentialsRequest(@NotBlank @Email @Size(max = 254) String email,
                                 @NotNull @Size(min = 12, max = 72) String password) {
    public CredentialsRequest {
        if (email != null) email = email.strip();
    }
    @AssertTrue(message = "Password must be at most 72 UTF-8 bytes")
    public boolean isPasswordWithinByteLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
    @Override public String toString() { return "CredentialsRequest[redacted]"; }
}

