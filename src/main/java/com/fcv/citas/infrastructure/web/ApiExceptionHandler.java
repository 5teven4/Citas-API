package com.fcv.citas.infrastructure.web;

import com.fcv.citas.application.auth.DuplicateResourceException;
import com.fcv.citas.application.auth.InvalidCredentialsException;
import com.fcv.citas.application.auth.InvalidRefreshTokenException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError duplicate(DuplicateResourceException exception) { return error(HttpStatus.CONFLICT, exception.getMessage()); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiError integrity(DataIntegrityViolationException exception) { return error(HttpStatus.CONFLICT, "El email o documento ya está registrado"); }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidRefreshTokenException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiError unauthorized(RuntimeException exception) { return error(HttpStatus.UNAUTHORIZED, exception.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = exception.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(error -> error.getField(), error -> error.getDefaultMessage(), (first, ignored) -> first));
        return Map.of("timestamp", Instant.now().toString(), "status", HttpStatus.BAD_REQUEST.value(), "errors", fields);
    }

    private ApiError error(HttpStatus status, String message) { return new ApiError(Instant.now(), status.value(), message); }
    record ApiError(Instant timestamp, int status, String message) { }
}
