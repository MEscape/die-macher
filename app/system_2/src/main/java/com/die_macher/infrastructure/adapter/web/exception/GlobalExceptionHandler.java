package com.die_macher.infrastructure.adapter.web.exception;

import com.die_macher.domain.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TIMESTAMP = "timestamp";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";

    @ExceptionHandler(DataProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleDataProcessingException(
            DataProcessingException ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, Instant.now());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(DETAILS, request.getDescription(false));
        errorDetails.put(ERROR, "Data Processing Error");

        LOGGER.error("Unhandled DataProcessingException occurred", ex);

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, Instant.now());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(DETAILS, request.getDescription(false));
        errorDetails.put(ERROR, "Invalid Request");

        LOGGER.error("IllegalArgumentException occurred", ex);

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDataNotFoundException(
            DataNotFoundException ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, Instant.now());
        errorDetails.put(MESSAGE, ex.getMessage());
        errorDetails.put(DETAILS, request.getDescription(false));
        errorDetails.put(ERROR, "Data Not Found");

        LOGGER.warn("DataNotFoundException occurred", ex);

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put(TIMESTAMP, Instant.now());
        errorDetails.put(MESSAGE, "An unexpected error occurred");
        errorDetails.put(DETAILS, request.getDescription(false));
        errorDetails.put(ERROR, "Internal Server Error");

        LOGGER.error("Unhandled Exception occurred", ex);

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
