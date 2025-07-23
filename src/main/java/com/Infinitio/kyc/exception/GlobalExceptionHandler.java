package com.Infinitio.kyc.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OurException.class)
    public ResponseEntity<ApiErrorResponse> handleOurException(OurException ex) {
        logger.error("Business logic error: ", ex);
        return new ResponseEntity<>(new ApiErrorResponse("Business Error", ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception ex) {
        logger.error("Unexpected error occurred: ", ex);
        return new ResponseEntity<>(new ApiErrorResponse("Internal Server Error", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}