package com.die_macher.application.transformer;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.robot.RobotData;
import com.die_macher.domain.model.robot.RobotStatus;
import com.die_macher.domain.model.robot.RobotTask;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class RobotDataTransformer {

  private final ObjectMapper objectMapper;

  public RobotDataTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public RobotData transformRawMessage(String rawMessage) {
    try {
      // Parse JSON or other format
      var jsonNode = objectMapper.readTree(rawMessage);

      return parseSingleMessage(jsonNode);

    } catch (Exception e) {
      throw new DataProcessingException("Failed to transform message: " + rawMessage, e);
    }
  }

  private RobotData parseSingleMessage(JsonNode node) {
    return RobotData.builder()
        .robotStatus(RobotStatus.valueOf(node.path("robotStatus").asText().toUpperCase()))
        .robotTask(RobotTask.valueOf(node.path("robotTask").asText().toUpperCase()))
        .color(node.path("color").asText().toUpperCase())
        .timestamp(Instant.parse(node.path("timestamp").asText()))
        .build();
  }
}
