package pl.exceptionhandled.repetitio.shared.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String code;

    protected BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}