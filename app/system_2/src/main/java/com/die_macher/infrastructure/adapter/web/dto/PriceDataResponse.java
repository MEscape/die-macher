package com.die_macher.infrastructure.adapter.web.dto;

import com.die_macher.domain.model.price.PriceData;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import lombok.Data;

@Data
public class PriceDataResponse {

  @JsonProperty("startTimestamp")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private Instant startTimestamp;

  @JsonProperty("endTimestamp")
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",
      timezone = "UTC")
  private Instant endTimestamp;

  @JsonProperty("totalCost")
  private double totalCost;

  @JsonProperty("priceInEurPerKwh")
  private double priceInEurPerKwh;

  @JsonProperty("startTimeFormatted")
  private String startTimeFormatted;

  @JsonProperty("endTimeFormatted")
  private String endTimeFormatted;

  @JsonProperty("metadata")
  private Map<String, String> metadata;

  public static PriceDataResponse from(PriceData priceData) {
    PriceDataResponse response = new PriceDataResponse();
    response.setStartTimestamp(priceData.startTimestamp());
    response.setEndTimestamp(priceData.endTimestamp());
    response.setTotalCost(priceData.totalCost());
    response.setPriceInEurPerKwh(priceData.priceInEurPerKwh());
    response.setStartTimeFormatted(priceData.startTimeFormatted());
    response.setEndTimeFormatted(priceData.endTimeFormatted());
    response.setMetadata(priceData.metadata());
    return response;
  }
}
