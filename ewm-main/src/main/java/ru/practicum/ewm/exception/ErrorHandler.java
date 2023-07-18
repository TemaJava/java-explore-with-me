package ru.practicum.ewm.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlerNotFoundException(final ObjectNotFoundException e) {
        return new ErrorResponse("Object not found 404", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIntegrityException(final ValidationException e) {
        return new ErrorResponse("Validation Exception", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIntegrityException(final BadRequestException e) {
        return new ErrorResponse("Bad request", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIntegrityException(DataAccessException exception) {
        return new ErrorResponse("Data exception", exception.getMessage());
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {

        private String error;

        private String description;
    }
}
