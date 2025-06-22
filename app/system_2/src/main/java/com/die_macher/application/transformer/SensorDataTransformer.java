package com.die_macher.application.transformer;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.model.SensorType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;

@Component
public class SensorDataTransformer {

  private final ObjectMapper objectMapper;

  public SensorDataTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<SensorData> transformRawMessage(String rawMessage) {
    try {
      // Parse JSON or other format
      var jsonNode = objectMapper.readTree(rawMessage);

      return jsonNode.has("sensors")
          ? parseBatchMessage(jsonNode)
          : List.of(parseSingleMessage(jsonNode));

    } catch (Exception e) {
      throw new DataProcessingException("Failed to transform message: " + rawMessage, e);
    }
  }

  private SensorData parseSingleMessage(JsonNode node) {
    return SensorData.builder()
        .sensorId(node.path("sensorId").asText())
        .type(SensorType.valueOf(node.path("type").asText().toUpperCase()))
        .value(node.path("value").asDouble())
        .unit(node.path("unit").asText())
        .timestamp(Instant.parse(node.path("timestamp").asText()))
        .build();
  }

  private List<SensorData> parseBatchMessage(JsonNode node) {
    // Implementation for batch processing
    return StreamSupport.stream(node.path("sensors").spliterator(), false)
        .map(this::parseSingleMessage)
        .toList();
  }
}
