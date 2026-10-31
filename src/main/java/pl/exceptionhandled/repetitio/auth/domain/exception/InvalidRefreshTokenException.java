package pl.exceptionhandled.repetitio.auth.domain.exception;

import pl.exceptionhandled.repetitio.shared.exception.BusinessException;

public class InvalidRefreshTokenException extends BusinessException {
    public InvalidRefreshTokenException() {
        super("INVALID_REFRESH_TOKEN", "refresh token is invalid");
    }
}
