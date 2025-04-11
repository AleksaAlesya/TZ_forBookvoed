package by.aleksabrakor.tzForBookvoed.aspect;


import by.aleksabrakor.tzForBookvoed.exception.ErrorResponse;
import by.aleksabrakor.tzForBookvoed.exception.NotCreatedException;
import by.aleksabrakor.tzForBookvoed.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    private ResponseEntity<ErrorResponse> handException(NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Object was not found: " + e.getMessage(),
                Timestamp.valueOf(LocalDateTime.now())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotCreatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private ResponseEntity<ErrorResponse> handException(NotCreatedException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Was not create:  " + e.getMessage(),
                Timestamp.valueOf(LocalDateTime.now())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed: " + errorMessage,
                Timestamp.valueOf(LocalDateTime.now())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
