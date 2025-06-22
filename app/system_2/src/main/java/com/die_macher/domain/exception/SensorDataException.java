package com.die_macher.domain.exception;

public class SensorDataException extends RuntimeException {

  public SensorDataException(String message) {
    super(message);
  }

  public SensorDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public SensorDataException(Throwable cause) {
    super(cause);
  }
}
