package org.practice.universal_directory_constructor.error.exception;

import org.apache.coyote.BadRequestException;
import org.practice.universal_directory_constructor.error.dto.ExceptionResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Ваше кастомное исключение должно наследоваться от RuntimeException
    // Создайте класс BadRequestException:
    // public class BadRequestException extends RuntimeException { ... }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex) {
        ExceptionResponse response = new ExceptionResponse(
                ex.getMessage(),
                "BAD_REQUEST",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ПРАВИЛЬНАЯ обработка ошибок валидации @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {



        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)  // Берем ТОЛЬКО сообщение, без названия поля
                .orElse("Validation failed");

        ExceptionResponse response = new ExceptionResponse(
                message,  // "fields[0]: Invalid directory id value for directory type"
                "BAD_REQUEST",  // Исправлено с NOT_FOUND на BAD_REQUEST
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Обработка ошибок парсинга JSON
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : "Invalid JSON format";

        ExceptionResponse response = new ExceptionResponse(
                message,
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Обработка всех остальных исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleAllExceptions(Exception ex) {
        ExceptionResponse response = new ExceptionResponse(
                "Internal server error",
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now()
        );

        // Логируем полную ошибку для отладки
        logger.error("Unhandled exception", ex);

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}