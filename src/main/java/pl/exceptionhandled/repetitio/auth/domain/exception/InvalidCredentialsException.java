package pl.exceptionhandled.repetitio.auth.domain.exception;

import pl.exceptionhandled.repetitio.shared.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(
                "INVALID_CREDENTIALS",
                "Invalid email or password"
        );
    }
}