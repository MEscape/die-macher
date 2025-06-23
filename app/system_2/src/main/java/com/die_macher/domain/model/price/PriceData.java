package com.die_macher.domain.model.price;

import java.time.Instant;
import java.util.Map;

public record PriceData(
        Instant startTimestamp,
        Instant endTimestamp,
        double marketprice,
        double priceInEurPerKwh,
        String unit,
        String startTimeFormatted,
        String endTimeFormatted,
        Map<String, String> metadata // optional, falls du Meta-Daten möchtest
) {

  public PriceData(
          Instant startTimestamp,
          Instant endTimestamp,
          double marketprice,
          double priceInEurPerKwh,
          String unit,
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
    this.marketprice = marketprice;
    this.priceInEurPerKwh = priceInEurPerKwh;
    this.unit = unit != null ? unit : "Eur/MWh";

    this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
  }

  // Konstruktor koji prima timestamp u milisekundama (kao što dolazi iz JSON-a)
  public PriceData(
          long startTimestampMillis,
          long endTimestampMillis,
          double marketprice,
          double priceInEurPerKwh,
          String unit,
          String startTimeFormatted,
          String endTimeFormatted,
          Map<String, String> metadata) {
    this(
            Instant.ofEpochMilli(startTimestampMillis),
            Instant.ofEpochMilli(endTimestampMillis),
            marketprice,
            priceInEurPerKwh,
            unit,
            startTimeFormatted,
            endTimeFormatted,
            metadata
    );
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
    private double marketprice;
    private double priceInEurPerKwh;
    private String unit = "Eur/MWh";
    private Map<String, String> metadata;

    public Builder startTimestamp(Instant startTimestamp) {
      this.startTimestamp = startTimestamp;
      return this;
    }

    public Builder startTimestamp(long startTimestampMillis) {
      this.startTimestamp = Instant.ofEpochMilli(startTimestampMillis);
      return this;
    }

    public Builder endTimestamp(Instant endTimestamp) {
      this.endTimestamp = endTimestamp;
      return this;
    }

    public Builder endTimestamp(long endTimestampMillis) {
      this.endTimestamp = Instant.ofEpochMilli(endTimestampMillis);
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

    public Builder marketprice(double marketprice) {
      this.marketprice = marketprice;
      return this;
    }

    public Builder priceInEurPerKwh(double priceInEurPerKwh) {
      this.priceInEurPerKwh = priceInEurPerKwh;
      return this;
    }

    public Builder unit(String unit) {
      this.unit = unit;
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
              marketprice,
              priceInEurPerKwh,
              unit,
              startTimeFormatted,
              endTimeFormatted,
              metadata);
    }
  }
}