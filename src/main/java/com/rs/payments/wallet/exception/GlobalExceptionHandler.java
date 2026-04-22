package com.rs.payments.wallet.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final String KEY_ERROR = "error";
    private static final String KEY_MESSAGE = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(KEY_ERROR, "Validation failed");
        body.put(KEY_MESSAGE, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(KEY_ERROR, "Not found");
        body.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        Map<String, String> body = new HashMap<>();
        body.put(KEY_ERROR, "Bad request");
        body.put(KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<Map<String, String>> handleConflict(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put(KEY_ERROR, "Conflict");
        body.put(KEY_MESSAGE, "User with the same username or email already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
