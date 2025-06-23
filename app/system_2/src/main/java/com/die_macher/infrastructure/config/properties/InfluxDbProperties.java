package com.die_macher.infrastructure.config.properties;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "influxdb")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InfluxDbProperties {

  @NotNull private String url = "http://localhost:8086";

  @NotNull private String token;

  @NotNull private String organization;

  @NotNull private String bucket;

  private Duration connectionTimeout = Duration.ofSeconds(10);
  private Duration readTimeout = Duration.ofSeconds(30);
}
