package com.ll.apigateway.model.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
  private final HttpStatus status;

  public CustomException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }

  // 자주 사용되는 예외를 위한 팩토리 메서드
  public static CustomException unauthorized(String message) {
    return new CustomException(message, HttpStatus.UNAUTHORIZED);
  }

  public static CustomException forbidden(String message) {
    return new CustomException(message, HttpStatus.FORBIDDEN);
  }

  public static CustomException notFound(String message) {
    return new CustomException(message, HttpStatus.NOT_FOUND);
  }

  public static CustomException badRequest(String message) {
    return new CustomException(message, HttpStatus.BAD_REQUEST);
  }
}