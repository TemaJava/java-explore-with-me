package ru.practicum.ewm.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIntegrityException(final BadRequestException e) {
        return new ErrorResponse("Bad request", e.getMessage());
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String error;
        private String description;
    }
}
