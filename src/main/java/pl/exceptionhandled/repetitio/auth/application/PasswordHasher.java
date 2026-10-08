package pl.exceptionhandled.repetitio.auth.application;
public interface PasswordHasher {
    String hash(String password);
    boolean matches(String password, String hash);
}

