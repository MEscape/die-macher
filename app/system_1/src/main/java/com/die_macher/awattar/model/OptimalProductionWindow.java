package com.die_macher.awattar.model;

import lombok.Getter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class OptimalProductionWindow {
    private final long startTimestamp;
    private final long endTimestamp;
    private final List<MarketData.MarketPrice> prices;
    private final double totalCost;
    private final double priceInEurPerKwh;

    public OptimalProductionWindow(long startTimestamp, long endTimestamp,
                                  List<MarketData.MarketPrice> prices, double totalCost) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.prices = prices;
        this.totalCost = totalCost;
        this.priceInEurPerKwh = 0.0; // Default value
    }
    
    public OptimalProductionWindow(long startTimestamp, long endTimestamp,
                                  List<MarketData.MarketPrice> prices, double totalCost, 
                                  double priceInEurPerKwh) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.prices = prices;
        this.totalCost = totalCost;
        this.priceInEurPerKwh = priceInEurPerKwh;
    }
    
    // Add formatted time getters for convenience
    public String getStartTimeFormatted() {
        return formatTimestamp(startTimestamp);
    }
    
    public String getEndTimeFormatted() {
        return formatTimestamp(endTimestamp);
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) {
            // Behandlung von ungültigen Timestamps
            return "Ungültiges Datum";
        }
        Instant instant = Instant.ofEpochMilli(timestamp);
        ZonedDateTime dateTime = instant.atZone(ZoneId.of("Europe/Berlin"));
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}