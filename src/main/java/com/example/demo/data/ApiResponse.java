package com.example.demo.data;

import java.time.Instant;

public class ApiResponse<T> {
  private int status;
  private String message;
  private T data;
  private long timestamp;

  public ApiResponse(int status, String message, T data) {
    this.status = status;
    this.message = message;
    this.data = data;
    this.timestamp = Instant.now().toEpochMilli();
  }

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(200, "Success", data);
  }

  public static <T> ApiResponse<T> error(int status, String message, T data) {
    return new ApiResponse<>(status, message, data);
  }

  public int getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
