package com.saivandan.crm.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
    Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(java.util.stream.Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a,b) -> a));
    return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "message", "Please correct the highlighted fields.", "errors", errors));
  }
  @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> invalid(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "message", ex.getMessage()));
  }
  @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> status(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(Map.of("timestamp", Instant.now(), "message", ex.getReason() == null ? "Request failed." : ex.getReason()));
  }
  @ExceptionHandler(AccessDeniedException.class) ResponseEntity<?> denied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("timestamp", Instant.now(), "message", "You do not have permission to perform this action."));
  }
  @ExceptionHandler(Exception.class) ResponseEntity<?> unexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("timestamp", Instant.now(), "message", "An unexpected error occurred."));
  }
}
