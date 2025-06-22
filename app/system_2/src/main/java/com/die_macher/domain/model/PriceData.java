package com.die_macher.domain.model;

import java.time.Instant;
import java.util.Map;

public record PriceData(
    Instant startTimestamp,
    Instant endTimestamp,
    double totalCost,
    double priceInEurPerKwh,
    String startTimeFormatted,
    String endTimeFormatted,
    Map<String, String> metadata // optional, falls du Meta-Daten möchtest
    ) {

  public PriceData(
      Instant startTimestamp,
      Instant endTimestamp,
      double totalCost,
      double priceInEurPerKwh,
      String startTimeFormatted,
      String endTimeFormatted,
      Map<String, String> metadata) {
    this.startTimestamp = validateTimestamp(startTimestamp, "startTimestamp");
    this.endTimestamp = validateTimestamp(endTimestamp, "endTimestamp");
    this.startTimeFormatted = startTimeFormatted;
    this.endTimeFormatted = endTimeFormatted;
    if (endTimestamp.isBefore(startTimestamp)) {
      throw new IllegalArgumentException("endTimestamp must be after startTimestamp");
    }
    this.totalCost = totalCost;
    this.priceInEurPerKwh = priceInEurPerKwh;

    this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
  }

  private static Instant validateTimestamp(Instant timestamp, String fieldName) {
    if (timestamp == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
    return timestamp;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Instant startTimestamp;
    private Instant endTimestamp;
    private String startTimeFormatted;
    private String endTimeFormatted;
    private double totalCost;
    private double priceInEurPerKwh;
    private Map<String, String> metadata;

    public Builder startTimestamp(Instant startTimestamp) {
      this.startTimestamp = startTimestamp;
      return this;
    }

    public Builder endTimestamp(Instant endTimestamp) {
      this.endTimestamp = endTimestamp;
      return this;
    }

    public Builder startTimeFormatted(String startTimeFormatted) {
      this.startTimeFormatted = startTimeFormatted;
      return this;
    }

    public Builder endTimeFormatted(String endTimeFormatted) {
      this.endTimeFormatted = endTimeFormatted;
      return this;
    }

    public Builder totalCost(double totalCost) {
      this.totalCost = totalCost;
      return this;
    }

    public Builder priceInEurPerKwh(double priceInEurPerKwh) {
      this.priceInEurPerKwh = priceInEurPerKwh;
      return this;
    }

    public Builder metadata(Map<String, String> metadata) {
      this.metadata = metadata;
      return this;
    }

    public PriceData build() {
      return new PriceData(
          startTimestamp,
          endTimestamp,
          totalCost,
          priceInEurPerKwh,
          startTimeFormatted,
          endTimeFormatted,
          metadata);
    }
  }
}
