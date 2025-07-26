package com.example.demo.excpetion;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.example.demo.data.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
    ex.printStackTrace(); // log to stderr, or use logger
    return buildErrorResponse(500, "Internal Server Error", null);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Object>> handleMissingParam(
      MissingServletRequestParameterException ex) {
    return buildErrorResponse(400, "Missing request parameter",
        Map.of("param", ex.getParameterName()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    return buildErrorResponse(400, "Parameter type mismatch", Map.of("param", ex.getName()));
  }

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ApiResponse<Object>> handleRateLimitExceeded(
      RateLimitExceededException ex) {
    return ResponseEntity.status(429)
        .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
        .body(ApiResponse.error(429, ex.getMessage(),
            Map.of("allowed", false, "retryAfter", ex.getRetryAfterSeconds())));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Object>> handleIllegalArg(IllegalArgumentException ex) {
    return buildErrorResponse(400, ex.getMessage(), null);
  }

  private ResponseEntity<ApiResponse<Object>> buildErrorResponse(int status, String msg,
      Object details) {
    return ResponseEntity.status(status).body(ApiResponse.error(status, msg, details));
  }
}
