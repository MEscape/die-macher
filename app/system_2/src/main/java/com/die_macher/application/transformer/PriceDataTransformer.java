package com.die_macher.application.transformer;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.price.PriceData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class PriceDataTransformer {

  private final ObjectMapper objectMapper;

  public PriceDataTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public PriceData transformRawMessage(String rawMessage) {
    try {
      // Parse JSON or other format
      var jsonNode = objectMapper.readTree(rawMessage);

      return parseSingleMessage(jsonNode);

    } catch (Exception e) {
      throw new DataProcessingException("Failed to transform message: " + rawMessage, e);
    }
  }

  private PriceData parseSingleMessage(JsonNode node) {
    return PriceData.builder()
        .startTimestamp(Instant.parse(node.path("startTimestamp").asText()))
        .endTimestamp(Instant.parse(node.path("endTimestamp").asText()))
        .startTimeFormatted(node.path("startTimeFormatted").asText())
        .endTimeFormatted(node.path("endTimeFormatted").asText())
        .totalCost(node.path("totalCost").asDouble())
        .priceInEurPerKwh(node.path("priceInEurPerKwh").asDouble())
        .build();
  }
}
