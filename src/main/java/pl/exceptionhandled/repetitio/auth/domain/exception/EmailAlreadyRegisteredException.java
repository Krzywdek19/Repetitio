package pl.exceptionhandled.repetitio.auth.domain.exception;

import pl.exceptionhandled.repetitio.shared.exception.BusinessException;

public class EmailAlreadyRegisteredException extends BusinessException {

  public EmailAlreadyRegisteredException() {
    super(
            "EMAIL_ALREADY_REGISTERED",
            "Email is already registered"
    );
  }
}