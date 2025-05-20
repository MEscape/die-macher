package com.die_macher.awattar.service;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.die_macher.awattar.model.OptimalProductionWindow;

import jakarta.annotation.PostConstruct;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for retrieving and processing electricity market data from the Awattar API.
 * Provides functionality to fetch current and future market prices, and calculate
 * optimal production windows based on electricity costs.
 */
@Service
public class AwattarService {
    private static final Logger logger = LoggerFactory.getLogger(AwattarService.class);
    private static final double ENERGY_PER_PART = 0.2; // kWh per part
    private static final int PARTS_PER_HOUR = 5; // Parts per hour
    private static final int PRODUCTION_HOURS = 3; // Required production time in hours
    
    private final RestTemplate restTemplate;
    MarketData cachedMarketData;
    private OptimalProductionWindow optimalWindow;

    /**
     * Constructs an AwattarService with the required RestTemplate.
     * 
     * @param restTemplate The RestTemplate used for API calls
     */
    @Autowired
    public AwattarService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Initializes the service on application startup.
     * If it's after 1 PM, attempts to load market data for tomorrow.
     */
    @PostConstruct
    public void initOnStartup() {
        // Check if it's after 1 PM
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Vienna"));
        if (now.isAfter(LocalTime.of(13, 0))) {
            logger.info("Starting initialization: It's after 1 PM, attempting to load market data for tomorrow...");
            boolean success = false;
            int maxTries = 30; 
            int tries = 0;
            while (!success && tries < maxTries) {
                MarketData data = fetchTomorrowMarketData();
                if (data != null && data.getData() != null && data.getData().size() >= PRODUCTION_HOURS) {
                    cachedMarketData = data;
                    calculateOptimalProductionWindow();
                    success = true;
                    logger.info("Market data for tomorrow successfully loaded and processed.");
                } else {
                    tries++;
                    logger.warn("Market data for tomorrow not yet available, trying again in 30 seconds... (Attempt {}/{})", tries, maxTries);
                    try {
                        Thread.sleep(120000); // Wait 120 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (!success) {
                logger.error("Market data could not be loaded after {} attempts.", maxTries);
            }
        }
    }

    /**
     * Fetches market data for any given start time (always for 24h)
     * 
     * @param startMillis The start time in milliseconds since epoch
     * @return MarketData object containing price information or null if the request fails
     */
    public MarketData fetchMarketDataFor(long startMillis) {
        try {
            long endMillis = startMillis + 24 * 60 * 60 * 1000; // 24 hours later
            String url = "https://api.awattar.at/v1/marketdata?start=" + startMillis + "&end=" + endMillis;
            logger.info("Calling aWATTar API: {}", url);
            ResponseEntity<MarketData> response = restTemplate.getForEntity(url, MarketData.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching market data: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Fetches market data for the current hour and onwards.
     * 
     * @return MarketData object containing current price information
     */
    public MarketData fetchCurrentMarketData() {
        ZonedDateTime nowHour = ZonedDateTime.now(ZoneId.of("Europe/Vienna")).truncatedTo(ChronoUnit.HOURS);
        long startMillis = nowHour.toInstant().toEpochMilli();
        return fetchMarketDataFor(startMillis);
    }

    /**
     * Fetches electricity prices for tomorrow (starting at midnight)
     * 
     * @return MarketData object containing tomorrow's price information
     */
    public MarketData fetchTomorrowMarketData() {
        ZonedDateTime tomorrowMidnight = ZonedDateTime.now(ZoneId.of("Europe/Vienna"))
                .plusDays(1)
                .truncatedTo(ChronoUnit.DAYS);
        long tomorrowMillis = tomorrowMidnight.toInstant().toEpochMilli();
        return fetchMarketDataFor(tomorrowMillis);
    }

    /**
     * Updates market data once daily at 2 PM
     */
    @Scheduled(cron = "0 0 14 * * *")
    public void updateMarketData() {
        logger.info("Updating market data for tomorrow...");
        cachedMarketData = fetchTomorrowMarketData();
        calculateOptimalProductionWindow();
    }

    /**
     * Calculates the optimal production window (3 consecutive hours with the lowest electricity costs)
     */
    private void calculateOptimalProductionWindow() {
        if (cachedMarketData == null || cachedMarketData.getData() == null || cachedMarketData.getData().size() < PRODUCTION_HOURS) {
            logger.warn("Insufficient market data for calculating the optimal production window");
            return;
        }

        List<MarketPrice> prices = cachedMarketData.getData();
        double lowestCost = Double.MAX_VALUE;
        int bestStartIndex = 0;

        // Find the cheapest time window of PRODUCTION_HOURS consecutive hours
        for (int i = 0; i <= prices.size() - PRODUCTION_HOURS; i++) {
            double windowCost = 0;
            for (int j = i; j < i + PRODUCTION_HOURS; j++) {
                windowCost += prices.get(j).getMarketprice();
            }

            if (windowCost < lowestCost) {
                lowestCost = windowCost;
                bestStartIndex = i;
            }
        }

        // Create the optimal production window
        MarketPrice startPrice = prices.get(bestStartIndex);
        MarketPrice endPrice = prices.get(bestStartIndex + PRODUCTION_HOURS - 1);
        
        List<MarketPrice> windowPrices = new ArrayList<>();
        for (int i = bestStartIndex; i < bestStartIndex + PRODUCTION_HOURS; i++) {
            windowPrices.add(prices.get(i));
        }
        
        double totalCost = calculateProductionCost(windowPrices);
        
        optimalWindow = new OptimalProductionWindow(
                startPrice.getStart_timestamp(),
                endPrice.getEnd_timestamp(),
                windowPrices,
                totalCost
        );
        
        logger.info("Optimal production window calculated: {} to {}, Cost: {} EUR",
                startPrice.getStartTimeFormatted(),
                endPrice.getEndTimeFormatted(),
                String.format("%.2f", totalCost));
    }

    /**
     * Calculates the production costs for a given time window
     * 
     * @param prices List of market prices in the time window
     * @return Total cost in EUR
     */
    private double calculateProductionCost(List<MarketPrice> prices) {
        double totalCost;
        int totalParts = PARTS_PER_HOUR * PRODUCTION_HOURS;
        double totalEnergy = ENERGY_PER_PART * totalParts;
        
        // Calculate the average price per kWh in the time window
        double avgPricePerKwh = prices.stream()
                .mapToDouble(MarketPrice::getPriceInEurPerKwh)
                .average()
                .orElse(0);
        
        // Total cost = Average price per kWh * Total energy consumption
        totalCost = avgPricePerKwh * totalEnergy;
        
        return totalCost;
    }

    /**
     * Returns the currently calculated optimal production window
     * 
     * @return OptimalProductionWindow or null if not yet calculated
     */
    public OptimalProductionWindow getOptimalProductionWindow() {
        if (optimalWindow == null) {
            // First load market data, then calculate!
            cachedMarketData = fetchTomorrowMarketData();
            calculateOptimalProductionWindow();
        }
        return optimalWindow;
    }

    /**
     * Calculates the energy cost for a single part based on the current electricity price.
     * Uses ENERGY_PER_PART (0.2 kWh) as the energy consumption per part.
     * 
     * @return The cost in EUR for producing a single part, or -1 if price data is unavailable
     */
    public double getCurrentPartCost() {
        MarketData marketData = fetchCurrentMarketData();
        if (marketData != null) {
            MarketPrice currentPrice = marketData.getCurrentPrice();
            if (currentPrice != null) {
                // Convert from EUR/MWh to EUR/kWh and multiply by energy per part
                double pricePerKwh = currentPrice.getPriceInEurPerKwh();
                return ENERGY_PER_PART * pricePerKwh;
            }
        }
        logger.warn("Could not calculate current part cost: price data unavailable");
        return -1;
    }
}