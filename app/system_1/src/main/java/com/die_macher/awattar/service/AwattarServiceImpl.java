package com.die_macher.awattar.service;

import com.die_macher.awattar.client.AwattarApiClient;
import com.die_macher.awattar.config.AwattarConfig;
import com.die_macher.awattar.dto.MarketDataDto;
import com.die_macher.awattar.mapper.AwattarMapper;
import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving and processing electricity market data from the Awattar API. Provides
 * functionality to fetch current and future market prices, and calculate optimal production windows
 * based on electricity costs.
 */
@Service
public class AwattarServiceImpl implements AwattarService {
  private static final Logger logger = LoggerFactory.getLogger(AwattarServiceImpl.class);
  private static final String VIENNA_TIMEZONE = "Europe/Vienna";

  private final AwattarApiClient apiClient;
  private final AwattarMapper mapper;
  private final AwattarConfig config;

  private MarketData cachedMarketData;
  private OptimalProductionWindow optimalWindow;

  /** Constructs an AwattarService with the required dependencies. */
  @Autowired
  public AwattarServiceImpl(
      AwattarApiClient apiClient, AwattarMapper mapper, AwattarConfig config) {
    this.apiClient = apiClient;
    this.mapper = mapper;
    this.config = config;
  }

  /**
   * Initializes the service on application startup. If it's after 1 PM, attempts to load market
   * data for tomorrow.
   */
  @PostConstruct
  public void initOnStartup() {
    // Check if it's after 1 PM
    LocalTime now = getNow();
    if (now.isAfter(LocalTime.of(13, 0))) {
      logger.info(
          "Starting initialization: It's after 1 PM, attempting to load market data for tomorrow...");
      boolean success = false;
      int maxTries = 30;
      int tries = 0;
      while (!success && tries < maxTries) {
        MarketData data = fetchTomorrowMarketData();
        if (data != null
            && data.getData() != null
            && data.getData().size() >= config.getProductionHours()) {
          cachedMarketData = data;
          calculateOptimalProductionWindow();
          success = true;
          logger.info("Market data for tomorrow successfully loaded and processed.");
        } else {
          tries++;
          logger.warn(
              "Market data for tomorrow not yet available, trying again in 120 seconds... (Attempt {}/{})",
              tries,
              maxTries);
          try {
            sleepFor(120000); // Use the extracted method instead of Thread.sleep directly
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
   * Protected method for sleeping that can be overridden in tests
   *
   * @param millis time to sleep in milliseconds
   * @throws InterruptedException if the thread is interrupted
   */
  protected void sleepFor(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

  /**
   * Protected method to get current time, can be overridden in tests
   *
   * @return current LocalTime in Vienna timezone
   */
  protected LocalTime getNow() {
    return LocalTime.now(ZoneId.of(VIENNA_TIMEZONE));
  }

  /**
   * Fetches market data for the current hour and onwards.
   *
   * @return MarketData object containing current price information
   */
  @Override
  public MarketData fetchCurrentMarketData() {
    ZonedDateTime nowHour =
        ZonedDateTime.now(ZoneId.of(VIENNA_TIMEZONE)).truncatedTo(ChronoUnit.HOURS);
    long startMillis = nowHour.toInstant().toEpochMilli();

    // Use the API client to fetch DTO and convert to domain model
    MarketDataDto dto = apiClient.fetchMarketDataFor(startMillis);
    return mapper.toModel(dto);
  }

  /**
   * Fetches electricity prices for tomorrow (starting at midnight)
   *
   * @return MarketData object containing tomorrow's price information
   */
  @Override
  public MarketData fetchTomorrowMarketData() {
    ZonedDateTime tomorrowMidnight =
        ZonedDateTime.now(ZoneId.of(VIENNA_TIMEZONE)).plusDays(1).truncatedTo(ChronoUnit.DAYS);
    long tomorrowMillis = tomorrowMidnight.toInstant().toEpochMilli();

    // Use the API client to fetch DTO and convert to domain model
    MarketDataDto dto = apiClient.fetchMarketDataFor(tomorrowMillis);
    return mapper.toModel(dto);
  }

  /** Updates market data once daily at 2 PM */
  @Scheduled(cron = "0 0 14 * * *")
  public void updateMarketData() {
    logger.info("Updating market data for tomorrow...");
    cachedMarketData = fetchTomorrowMarketData();
    calculateOptimalProductionWindow();
  }

  /**
   * Calculates the optimal production window (consecutive hours with the lowest electricity costs)
   */
  void calculateOptimalProductionWindow() {
    if (cachedMarketData == null
        || cachedMarketData.getData() == null
        || cachedMarketData.getData().size() < config.getProductionHours()) {
      logger.warn("Insufficient market data for calculating the optimal production window");
      return;
    }

    List<MarketPrice> prices = cachedMarketData.getData();
    double lowestCost = Double.MAX_VALUE;
    int bestStartIndex = 0;

    // Find the cheapest time window of PRODUCTION_HOURS consecutive hours
    for (int i = 0; i <= prices.size() - config.getProductionHours(); i++) {
      double windowCost = 0;
      for (int j = i; j < i + config.getProductionHours(); j++) {
        windowCost += prices.get(j).getMarketprice();
      }

      if (windowCost < lowestCost) {
        lowestCost = windowCost;
        bestStartIndex = i;
      }
    }

    // Create the optimal production window
    MarketPrice startPrice = prices.get(bestStartIndex);
    MarketPrice endPrice = prices.get(bestStartIndex + config.getProductionHours() - 1);

    List<MarketPrice> windowPrices = new ArrayList<>();
    for (int i = bestStartIndex; i < bestStartIndex + config.getProductionHours(); i++) {
      windowPrices.add(prices.get(i));
    }

    double totalCost = calculateProductionCost(windowPrices);
    double avgPricePerKwh =
        windowPrices.stream().mapToDouble(MarketPrice::getPriceInEurPerKwh).average().orElse(0);

    optimalWindow =
        new OptimalProductionWindow(
            startPrice.getStartTimestamp(),
            endPrice.getEndTimestamp(),
            windowPrices,
            totalCost,
            avgPricePerKwh);

    if (logger.isInfoEnabled()) {
      logger.info(
          "Optimal production window calculated: {} to {}, Cost: {} EUR",
          startPrice.getStartTimeFormatted(),
          endPrice.getEndTimeFormatted(),
          String.format("%.2f", totalCost));
    }
  }

  /**
   * Calculates the production costs for a given time window
   *
   * @param prices List of market prices in the time window
   * @return Total cost in EUR
   */
  private double calculateProductionCost(List<MarketPrice> prices) {
    double totalCost;
    int totalParts = config.getPartsPerHour() * config.getProductionHours();
    double totalEnergy = config.getEnergyPerPart() * totalParts;

    // Calculate the average price per kWh in the time window
    double avgPricePerKwh =
        prices.stream().mapToDouble(MarketPrice::getPriceInEurPerKwh).average().orElse(0);

    // Total cost = Average price per kWh * Total energy consumption
    totalCost = avgPricePerKwh * totalEnergy;

    return totalCost;
  }

  /**
   * Returns the currently calculated optimal production window
   *
   * @return OptimalProductionWindow or null if not yet calculated
   */
  @Override
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
   *
   * @return The cost in EUR for producing a single part, or -1 if price data is unavailable
   */
  @Override
  public double getCurrentPartCost() {
    MarketData marketData = fetchCurrentMarketData();
    if (marketData != null) {
      MarketPrice currentPrice = marketData.getCurrentPrice();
      if (currentPrice != null) {
        // Convert from EUR/MWh to EUR/kWh and multiply by energy per part
        double pricePerKwh = currentPrice.getPriceInEurPerKwh();
        return config.getEnergyPerPart() * pricePerKwh;
      }
    }
    logger.warn("Could not calculate current part cost: price data unavailable");
    return -1;
  }
}
