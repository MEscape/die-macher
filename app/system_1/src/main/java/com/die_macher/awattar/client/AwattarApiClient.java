package com.die_macher.awattar.client;

import com.die_macher.awattar.config.AwattarConfig;
import com.die_macher.awattar.dto.MarketDataDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AwattarApiClient {
  private static final Logger logger = LoggerFactory.getLogger(AwattarApiClient.class);

  private final RestTemplate restTemplate;
  private final AwattarConfig config;

  public AwattarApiClient(RestTemplate restTemplate, AwattarConfig config) {
    this.restTemplate = restTemplate;
    this.config = config;
  }

  public MarketDataDto fetchMarketDataFor(long startMillis) {
    try {
      long endMillis = startMillis + 24 * 60 * 60 * 1000; // 24 hours later
      String url = config.getApiBaseUrl() + "?start=" + startMillis + "&end=" + endMillis;
      logger.info("Calling aWATTar API: {}", url);
      ResponseEntity<MarketDataDto> response = restTemplate.getForEntity(url, MarketDataDto.class);
      return response.getBody();
    } catch (Exception e) {
      logger.error("Error fetching market data: {}", e.getMessage(), e);
      return null;
    }
  }
}
