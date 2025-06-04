package com.die_macher.awattar.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;

/**
 * Represents an optimal time window for electricity production based on market prices. Contains
 * information about the start and end times, associated prices, and total cost.
 */
@Getter
public class OptimalProductionWindow {
  private final long startTimestamp;
  private final long endTimestamp;
  private final List<MarketData.MarketPrice> prices;
  private final double totalCost;
  private final double priceInEurPerKwh;

  /**
   * Constructs an OptimalProductionWindow with the specified parameters.
   *
   * @param startTimestamp The start time of the optimal window in milliseconds since epoch
   * @param endTimestamp The end time of the optimal window in milliseconds since epoch
   * @param prices The list of market prices within this window
   * @param totalCost The total cost for production during this window
   */
  public OptimalProductionWindow(
      long startTimestamp,
      long endTimestamp,
      List<MarketData.MarketPrice> prices,
      double totalCost) {
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.prices = prices;
    this.totalCost = totalCost;
    this.priceInEurPerKwh = 0.0; // Default value
  }

  /**
   * Constructs an OptimalProductionWindow with the specified parameters including price per kWh.
   *
   * @param startTimestamp The start time of the optimal window in milliseconds since epoch
   * @param endTimestamp The end time of the optimal window in milliseconds since epoch
   * @param prices The list of market prices within this window
   * @param totalCost The total cost for production during this window
   * @param priceInEurPerKwh The price in EUR per kWh
   */
  public OptimalProductionWindow(
      long startTimestamp,
      long endTimestamp,
      List<MarketData.MarketPrice> prices,
      double totalCost,
      double priceInEurPerKwh) {
    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.prices = prices;
    this.totalCost = totalCost;
    this.priceInEurPerKwh = priceInEurPerKwh;
  }

  public String getStartTimeFormatted() {
    return formatTimestamp(startTimestamp);
  }

  public String getEndTimeFormatted() {
    return formatTimestamp(endTimestamp);
  }

  /**
   * Formats a timestamp to a human-readable date and time string.
   *
   * @param timestamp The timestamp in milliseconds
   * @return The formatted date and time string or "Invalid date" for invalid timestamps
   */
  private String formatTimestamp(long timestamp) {
    if (timestamp <= 0) {
      // Handling of invalid timestamps
      return "Invalid date";
    }
    Instant instant = Instant.ofEpochMilli(timestamp);
    ZonedDateTime dateTime = instant.atZone(ZoneId.of("Europe/Berlin"));
    return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
  }
}
