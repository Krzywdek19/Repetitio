package pl.exceptionhandled.repetitio.user.application;

public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException() { super("Email already registered"); }
}
