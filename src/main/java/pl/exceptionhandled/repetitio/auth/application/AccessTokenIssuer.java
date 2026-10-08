package pl.exceptionhandled.repetitio.auth.application;
import java.util.UUID;
public interface AccessTokenIssuer { TokenResponse issue(UUID userId); }

