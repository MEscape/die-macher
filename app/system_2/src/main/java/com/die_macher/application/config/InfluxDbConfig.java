package com.die_macher.application.config;

import com.die_macher.infrastructure.config.properties.InfluxDbProperties;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDbConfig {

  private final InfluxDbProperties properties;

  public InfluxDbConfig(InfluxDbProperties properties) {
    this.properties = properties;
  }

  @Bean
  public InfluxDBClient influxDBClient() {
    return InfluxDBClientFactory.create(
        properties.getUrl(),
        properties.getToken().toCharArray(),
        properties.getOrganization(),
        properties.getBucket());
  }
}
