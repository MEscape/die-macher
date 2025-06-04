package com.die_macher.awattar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "awattar")
@Getter
@Setter
public class AwattarConfig {
  private double energyPerPart = 0.2;
  private int partsPerHour = 5;
  private int productionHours = 3;
  private String apiBaseUrl = "https://api.awattar.at/v1/marketdata";
}
