package com.die_macher.infrastructure.adapter.web.dto;

import com.die_macher.domain.model.robot.RobotData;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.Data;

@Data
public class RobotDataResponse {

  @JsonProperty("robotStatus")
  private String robotStatus;

  @JsonProperty("robotTask")
  private String robotTask;

  @JsonProperty("color")
  private String color;

  @JsonProperty("timestamp")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private Instant timestamp;

  @JsonProperty("metadata")
  private Map<String, String> metadata;

  public static RobotDataResponse from(RobotData robotData) {
    RobotDataResponse response = new RobotDataResponse();
    response.setRobotTask(robotData.robotTask().name());
    response.setRobotStatus(robotData.robotStatus().name());
    response.setColor(robotData.color());
    response.setTimestamp(robotData.timestamp());
    response.setMetadata(robotData.metadata());
    return response;
  }
}
