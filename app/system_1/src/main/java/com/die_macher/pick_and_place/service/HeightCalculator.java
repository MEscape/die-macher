package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.config.RobotConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeightCalculator {
  private final RobotConfiguration.PhysicalConstants constants;

  @Autowired
  public HeightCalculator(RobotConfiguration config) {
    this.constants = config.physicalConstants();
  }

  public float calculateApproachHeight(int stackPosition) {
    return stackPosition * constants.cubeHeight() + constants.offset() + constants.absoluteFloor();
  }

  public float calculatePickupHeight(int stackPosition) {
    return stackPosition * constants.cubeHeight()
        + constants.absoluteFloor()
        - stackPosition * 0.75F;
  }

  public float calculateLiftHeight(float prevHeight, int stackPosition) {
    float rawLift = (stackPosition * constants.cubeHeight() + constants.cubeHeight()) - prevHeight;
    return Math.max(rawLift, 0f);
  }
}
