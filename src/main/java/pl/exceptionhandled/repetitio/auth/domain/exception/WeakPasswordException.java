package pl.exceptionhandled.repetitio.auth.domain.exception;

import pl.exceptionhandled.repetitio.shared.exception.BusinessException;

public class WeakPasswordException extends BusinessException {

    public WeakPasswordException(String message) {
        super("WEAK_PASSWORD", message);
    }
}