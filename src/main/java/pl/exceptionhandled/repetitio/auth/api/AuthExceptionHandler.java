package pl.exceptionhandled.repetitio.auth.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.exceptionhandled.repetitio.auth.domain.exception.EmailAlreadyRegisteredException;
import pl.exceptionhandled.repetitio.auth.domain.exception.InvalidCredentialsException;
import pl.exceptionhandled.repetitio.auth.domain.exception.WeakPasswordException;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleEmailAlreadyRegistered(
            EmailAlreadyRegisteredException exception
    ) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(WeakPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleWeakPassword(
            WeakPasswordException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setProperty("code", exception.getCode());

        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );

        problem.setProperty("code", exception.getCode());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(problem);
    }
}