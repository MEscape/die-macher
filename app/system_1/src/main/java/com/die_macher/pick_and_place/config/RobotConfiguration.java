package com.die_macher.pick_and_place.config;

import com.die_macher.pick_and_place.model.Position;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "dobot.movement")
public record RobotConfiguration(
    @Valid @NotNull MovementProfile fastMovement,
    @Valid @NotNull MovementProfile slowMovement,
    @Valid @NotNull RobotPositions positions,
    @Valid @NotNull PhysicalConstants physicalConstants) {

  public record MovementProfile(
      @Min(1) @Max(1000) int xyzVelocity,
      @Min(1) @Max(1000) int rVelocity,
      @Min(1) @Max(1000) int xyzAcceleration,
      @Min(1) @Max(1000) int rAcceleration) {}

  public record RobotPositions(
      @Valid @NotNull Position startPoint,
      @Valid @NotNull Position pickupPoint,
      @Valid @NotNull Position camera,
      @Valid @NotNull Position red,
      @Valid @NotNull Position green,
      @Valid @NotNull Position blue,
      @Valid @NotNull Position yellow) {}

  public record PhysicalConstants(
      @Valid @NotNull float absoluteFloor,
      @Valid @NotNull float cubeHeight,
      @Valid @NotNull float offset,
      @Valid @NotNull float maxHeight) {}
}
