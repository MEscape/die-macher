package com.die_macher.infrastructure.config.properties;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "spring.integration.mqtt")
@Configuration
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MqttProperties {

  @Valid @NotNull @NestedConfigurationProperty private Broker broker = new Broker();

  @Data
  public static class Broker {
    @NotNull private String url = "tcp://localhost:1883";

    @NotNull private String clientId = "system2-processor";

    private String username;
    private String password;

    @NotNull private String topic = "system2/data";

    private int qos = 1;
  }
}
