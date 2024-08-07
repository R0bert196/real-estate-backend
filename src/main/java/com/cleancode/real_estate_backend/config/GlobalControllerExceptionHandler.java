package com.cleancode.real_estate_backend.config;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(Exception e, WebRequest request) {
        log.error("An unexpected error occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(buildResponse(e, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value={IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(Exception e, WebRequest request) {
        log.error(" IllegalArgumentException occurred: {}", e.getMessage(), e);
        return new ResponseEntity<>(buildResponse(e, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }



    private Map<String, Object> buildResponse(Exception ex, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", ex.getMessage());
        response.put("details", ex.getLocalizedMessage());
        return response;
    }
}
