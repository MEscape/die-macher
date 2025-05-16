package com.die_macher.awattar.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Setter
@Getter
public class MarketData {
    private String object;
    private List<MarketPrice> data;

    public MarketPrice getFirstPrice() {
        if (data != null && !data.isEmpty()) {
            return data.get(0);
        }
        return null;
    }
    @Getter
    @Setter
    public static class MarketPrice {
        @Getter
        @Setter
        private long startTimestamp;
        @Getter
        @Setter
        private long end_timestamp;
        @Getter
        @Setter
        private double marketprice;
        @Getter
        @Setter
        private String unit;

        // Getter und Setter
        public long getStart_timestamp() {
            return startTimestamp;
        }

        // Hilfsmethoden zur Formatierung
        public String getStartTimeFormatted() {
            return formatTimestamp(startTimestamp);
        }

        public String getEndTimeFormatted() {
            return formatTimestamp(end_timestamp);
        }

        private String formatTimestamp(long timestamp) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.of("Europe/Vienna"));
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        // Umrechnung von EUR/MWh in EUR/kWh
        public double getPriceInEurPerKwh() {
            return marketprice / 1000.0;
        }

    }
}