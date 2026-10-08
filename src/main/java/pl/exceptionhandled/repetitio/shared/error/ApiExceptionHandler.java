package pl.exceptionhandled.repetitio.shared.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ProblemDetail application(ApiException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(exception.status()), exception.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class})
    ProblemDetail invalid(Exception exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail optimisticConflict() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Resource changed; reload before retrying");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrityConflict() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Operation conflicts with current resource state");
    }
}

