package com.die_macher.domain.model.sensor;

import java.time.Instant;
import java.util.Map;

public record SensorData(
    String sensorId,
    SensorType type,
    Double value,
    String unit,
    Instant timestamp,
    Map<String, String> metadata) {
  public SensorData(
      String sensorId,
      SensorType type,
      Double value,
      String unit,
      Instant timestamp,
      Map<String, String> metadata) {
    this.sensorId = validateSensorId(sensorId);
    this.type = validateType(type);
    this.value = validateValue(value);
    this.unit = unit;
    this.timestamp = timestamp != null ? timestamp : Instant.now();
    this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
  }

  // Validation methods...
  private String validateSensorId(String sensorId) {
    if (sensorId == null || sensorId.trim().isEmpty()) {
      throw new IllegalArgumentException("Sensor ID cannot be null or empty");
    }
    return sensorId.trim();
  }

  private SensorType validateType(SensorType type) {
    if (type == null) {
      throw new IllegalArgumentException("Sensor type cannot be null");
    }
    return type;
  }

  private Double validateValue(Double value) {
    if (value == null || value.isNaN()) {
      throw new IllegalArgumentException("Sensor value cannot be null or NaN");
    }
    return value;
  }

  // Builder pattern for easier construction
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String sensorId;
    private SensorType type;
    private Double value;
    private String unit;
    private Instant timestamp;
    private Map<String, String> metadata;

    public Builder sensorId(String sensorId) {
      this.sensorId = sensorId;
      return this;
    }

    public Builder type(SensorType type) {
      this.type = type;
      return this;
    }

    public Builder value(Double value) {
      this.value = value;
      return this;
    }

    public Builder unit(String unit) {
      this.unit = unit;
      return this;
    }

    public Builder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder metadata(Map<String, String> metadata) {
      this.metadata = metadata;
      return this;
    }

    public SensorData build() {
      return new SensorData(sensorId, type, value, unit, timestamp, metadata);
    }
  }
}
