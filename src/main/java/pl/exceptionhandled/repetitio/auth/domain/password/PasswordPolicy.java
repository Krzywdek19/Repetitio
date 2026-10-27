package pl.exceptionhandled.repetitio.auth.domain.password;

import pl.exceptionhandled.repetitio.auth.domain.exception.WeakPasswordException;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 128;

    public void validate(String password) {

        if (password == null) {
            throw new WeakPasswordException("Password is required");
        }

        int length = password.codePointCount(0, password.length());

        if (length < MIN_LENGTH) {
            throw new WeakPasswordException(
                    "Password must contain at least 15 characters"
            );
        }

        if (length > MAX_LENGTH) {
            throw new WeakPasswordException(
                    "Password cannot exceed 128 characters"
            );
        }
    }
}