package com.csi.common.web;

import com.csi.common.api.ErrorResponse;
import com.csi.common.domain.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Traduit les exceptions techniques et metier en reponses HTTP lisibles par l application mobile.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> business(BusinessException ex) {
        HttpStatus status = businessStatus(ex.code());
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ex.code(), ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ErrorResponse.of("VALIDATION_ERROR", "Invalid request payload", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> constraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), Map.of()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("ACCESS_DENIED", "Accès refusé", Map.of()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", Map.of()));
    }

    private HttpStatus businessStatus(String code) {
        if (code == null) return HttpStatus.UNPROCESSABLE_ENTITY;
        if (code.endsWith("_NOT_FOUND")) return HttpStatus.NOT_FOUND;
        if (code.contains("ACCESS_DENIED") || code.contains("READ_ONLY")) return HttpStatus.FORBIDDEN;
        if (code.contains("CONFLICT") || code.contains("ALREADY_EXISTS") || code.contains("LOCKED")) return HttpStatus.CONFLICT;
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
