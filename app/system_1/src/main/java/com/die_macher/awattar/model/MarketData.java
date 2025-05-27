package com.die_macher.awattar.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Represents electricity market data from Awattar API.
 * Contains a list of market prices for different time periods.
 */
@Setter
@Getter
public class MarketData {
    private String object;
    private List<MarketPrice> data;

    /**
     * Returns the current market price from the data list.
     * 
     * @return The first market price in the list or null if the list is empty
     */
    public MarketPrice getCurrentPrice() {
        if (data != null && !data.isEmpty()) {
            return data.getFirst();
        }
        return null;
    }
    
    /**
     * Represents a single market price entry for a specific time period.
     */
    @Getter
    @Setter
    public static class MarketPrice {
        @Setter
        private long startTimestamp;
        @Setter
        private long endTimestamp;
        @Setter
        private double marketprice;
        @Setter
        private String unit;

        /**
         * Helper methods for formatting
         * Returns the formatted start time.
         * 
         * @return The start time formatted as "dd.MM.yyyy HH:mm"
         */
        public String getStartTimeFormatted() {
            return formatTimestamp(startTimestamp);
        }

        /**
         * Returns the formatted end time.
         * 
         * @return The end time formatted as "dd.MM.yyyy HH:mm"
         */
        public String getEndTimeFormatted() {
            return formatTimestamp(endTimestamp);
        }

        /**
         * Formats a timestamp to a human-readable date and time string.
         * 
         * @param timestamp The timestamp in milliseconds
         * @return The formatted date and time string
         */
        private String formatTimestamp(long timestamp) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    ZoneId.of("Europe/Vienna"));
            return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        }

        /**
         * Conversion from EUR/MWh to EUR/kWh
         * Returns the price in EUR per kWh.
         * 
         * @return The price converted from EUR/MWh to EUR/kWh
         */
        public double getPriceInEurPerKwh() {
            return marketprice / 1000.0;
        }
    }
}