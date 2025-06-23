package com.die_macher.application.transformer;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.price.PriceData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class PriceDataTransformer {

  private final ObjectMapper objectMapper;

  // Constants for field names
  private static final String FIELD_START_TIMESTAMP = "startTimestamp";
  private static final String FIELD_END_TIMESTAMP = "endTimestamp";
  private static final String FIELD_MARKET_PRICE = "marketprice";
  private static final String FIELD_PRICE_EUR_KWH = "priceInEurPerKwh";
  private static final String FIELD_START_TIME_FORMATTED = "startTimeFormatted";
  private static final String FIELD_END_TIME_FORMATTED = "endTimeFormatted";
  private static final String FIELD_UNIT = "unit";
  private static final String FIELD_METADATA = "metadata";
  private static final String FIELD_DATA = "data";

  public PriceDataTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public PriceData transformRawMessage(String rawMessage) {
    try {
      var jsonNode = objectMapper.readTree(rawMessage);
      return parseSingleMessage(jsonNode);
    } catch (Exception e) {
      throw new DataProcessingException("Failed to transform message: " + rawMessage, e);
    }
  }

  private PriceData parseSingleMessage(JsonNode node) {
    return PriceData.builder()
            .startTimestamp(Instant.parse(node.path(FIELD_START_TIMESTAMP).asText()))
            .endTimestamp(Instant.parse(node.path(FIELD_END_TIMESTAMP).asText()))
            .startTimeFormatted(node.path(FIELD_START_TIME_FORMATTED).asText())
            .endTimeFormatted(node.path(FIELD_END_TIME_FORMATTED).asText())
            .marketprice(node.path(FIELD_MARKET_PRICE).asDouble())
            .priceInEurPerKwh(node.path(FIELD_PRICE_EUR_KWH).asDouble())
            .build();
  }

  public List<PriceData> parseTcpResponse(byte[] response) throws JsonProcessingException {
    String responseString = new String(response, StandardCharsets.UTF_8);
    JsonNode responseJson = objectMapper.readTree(responseString);

    List<PriceData> priceDataList = new ArrayList<>();

    JsonNode dataNode = responseJson.get(FIELD_DATA);
    if (dataNode != null) {
      JsonNode dataArray = dataNode.get(FIELD_DATA);

      if (dataArray != null && dataArray.isArray()) {
        for (JsonNode item : dataArray) {
          PriceData priceData = parseSinglePriceData(item);
          priceDataList.add(priceData);
        }
      }
    }

    return priceDataList;
  }

  private PriceData parseSinglePriceData(JsonNode item) {
    return PriceData.builder()
            .startTimestamp(item.get(FIELD_START_TIMESTAMP).asLong())
            .endTimestamp(item.get(FIELD_END_TIMESTAMP).asLong())
            .marketprice(item.get(FIELD_MARKET_PRICE).asDouble())
            .priceInEurPerKwh(item.get(FIELD_PRICE_EUR_KWH).asDouble())
            .unit(item.has(FIELD_UNIT) ? item.get(FIELD_UNIT).asText() : "Eur/MWh")
            .startTimeFormatted(item.has(FIELD_START_TIME_FORMATTED) ?
                    item.get(FIELD_START_TIME_FORMATTED).asText() : null)
            .endTimeFormatted(item.has(FIELD_END_TIME_FORMATTED) ?
                    item.get(FIELD_END_TIME_FORMATTED).asText() : null)
            .metadata(parseMetadata(item))
            .build();
  }

  private Map<String, String> parseMetadata(JsonNode item) {
    Map<String, String> metadata = new HashMap<>();
    if (item.has(FIELD_METADATA)) {
      JsonNode metadataNode = item.get(FIELD_METADATA);
      metadataNode.fields().forEachRemaining(entry ->
              metadata.put(entry.getKey(), entry.getValue().asText())
      );
    }
    return metadata;
  }
}
