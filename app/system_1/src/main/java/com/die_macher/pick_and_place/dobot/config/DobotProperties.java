package com.die_macher.pick_and_place.dobot.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "dobot")
@Data
public class DobotProperties {

  @NotBlank private String portName;

  @Min(value = 1000)
  @Max(value = 10000)
  private int timeoutMillis;
}
