package com.die_macher.infrastructure.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.integration.tcp.server")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TcpServerProperties {

  @NotNull private Integer port = 9999;

  @Min(0)
  private Integer backlog = 100;

  @Min(0)
  private Integer soTimeout = 30000;

  @Min(2048)
  @Max(Integer.MAX_VALUE)
  private Integer maxMessageSize = 1024 * 1024;

  @Min(1)
  @Max(4)
  private Integer headerSize = 2;
}
