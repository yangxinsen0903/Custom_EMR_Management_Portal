package com.azure.csu.tiger.rm.api.exception;

import com.azure.csu.tiger.rm.api.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理自定义异常
    @ExceptionHandler(RmException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(RmException ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(ex.getStatus().value(), ex.getStatus().toString(),ex.getMessage(), request.getDescription(false));
        logger.warn("Custom exception: " + ex.getMessage(), ex);
        return new ResponseEntity<>(errorDetails, ex.getStatus());
    }

    // 处理验证异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.error("ValidationExceptions: " + ex.getMessage(), ex);
        ex.printStackTrace();
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 处理所有其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorDetails = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), ex.getMessage(), request.getDescription(false));
        logger.error("Global exception: " + ex.getMessage(), ex);
        ex.printStackTrace();
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
