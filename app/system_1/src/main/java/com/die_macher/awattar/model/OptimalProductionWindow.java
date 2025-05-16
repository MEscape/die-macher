package com.die_macher.awattar.model;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class OptimalProductionWindow {
    private final long startTimestamp;
    private final long endTimestamp;
    private final List<MarketData.MarketPrice> prices;
    private final double totalCost;

    public OptimalProductionWindow(long startTimestamp, long endTimestamp,
                                  List<MarketData.MarketPrice> prices, double totalCost) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.prices = prices;
        this.totalCost = totalCost;
    }

    private String formatTimestamp(long timestamp) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.of("Europe/Vienna"));
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}