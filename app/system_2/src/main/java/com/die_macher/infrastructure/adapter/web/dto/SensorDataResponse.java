package com.die_macher.infrastructure.adapter.web.dto;

import com.die_macher.domain.model.sensor.SensorData;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.Data;

@Data
public class SensorDataResponse {

  @JsonProperty("sensorId")
  private String sensorId;

  @JsonProperty("type")
  private String type;

  @JsonProperty("value")
  private Double value;

  @JsonProperty("unit")
  private String unit;

  @JsonProperty("timestamp")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private Instant timestamp;

  @JsonProperty("metadata")
  private Map<String, String> metadata;

  public static SensorDataResponse from(SensorData sensorData) {
    SensorDataResponse response = new SensorDataResponse();
    response.setSensorId(sensorData.sensorId());
    response.setType(sensorData.type().name());
    response.setValue(sensorData.value());
    response.setUnit(sensorData.unit());
    response.setTimestamp(sensorData.timestamp());
    response.setMetadata(sensorData.metadata());
    return response;
  }
}
