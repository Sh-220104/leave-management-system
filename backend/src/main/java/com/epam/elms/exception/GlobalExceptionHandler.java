package com.epam.elms.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusiness(BusinessException ex) {
        return new ResponseEntity<>(
            new ApiError(LocalDateTime.now(), ex.getMessage()),
            HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex) {
        return new ResponseEntity<>(
            new ApiError(LocalDateTime.now(), ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }

    record ApiError(LocalDateTime timestamp, String message) {}
}
