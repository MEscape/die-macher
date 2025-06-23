package com.die_macher.domain.exception;

public class DataProcessingException extends RuntimeException {

  public DataProcessingException(String message) {
    super(message);
  }

  public DataProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataProcessingException(Throwable cause) {
    super(cause);
  }
}
