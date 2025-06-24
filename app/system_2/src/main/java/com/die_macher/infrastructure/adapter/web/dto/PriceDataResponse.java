package com.die_macher.infrastructure.adapter.web.dto;

import com.die_macher.domain.model.price.PriceData;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  @JsonProperty("additionalPriceData")
  private List<PriceDataResponse> additionalPriceData;

  public static PriceDataResponse from(PriceData priceData) {
    PriceDataResponse response = new PriceDataResponse();
    response.setStartTimestamp(priceData.startTimestamp());
    response.setEndTimestamp(priceData.endTimestamp());
    response.setTotalCost(priceData.marketprice());
    response.setPriceInEurPerKwh(priceData.priceInEurPerKwh());
    response.setStartTimeFormatted(priceData.startTimeFormatted());
    response.setEndTimeFormatted(priceData.endTimeFormatted());
    response.setMetadata(priceData.metadata());
    return response;
  }

  public static List<PriceDataResponse> fromList(List<PriceData> priceDataList) {
    return priceDataList.stream()
            .map(PriceDataResponse::from)
            .collect(Collectors.toList());
  }
}
