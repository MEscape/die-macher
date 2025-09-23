package com.die_macher.pick_and_place.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PickAndPlaceResult {

  private LocalDateTime timestamp;

  private String color;

  private double temperature;

  private double humidity;

  private double energyCost;

  private boolean result;

  public PickAndPlaceResult(
      String color, double temperature, double humidity, double energyCost, boolean result) {}
}
