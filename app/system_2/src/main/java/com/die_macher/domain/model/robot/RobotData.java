package com.die_macher.domain.model.robot;

import java.time.Instant;
import java.util.Map;

public record RobotData(
    RobotTask robotTask,
    RobotStatus robotStatus,
    String color,
    Instant timestamp,
    Map<String, String> metadata) {

  public RobotData(
      RobotTask robotTask,
      RobotStatus robotStatus,
      String color,
      Instant timestamp,
      Map<String, String> metadata) {
    this.robotTask = validateRobotTask(robotTask);
    this.robotStatus = validateRobotStatus(robotStatus);
    this.color = color;
    this.timestamp = timestamp != null ? timestamp : Instant.now();

    this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
  }

  private RobotStatus validateRobotStatus(RobotStatus robotStatus) {
    if (robotStatus == null) {
      throw new IllegalArgumentException("Robot status cannot be null");
    }
    return robotStatus;
  }

  private RobotTask validateRobotTask(RobotTask robotTask) {
    if (robotTask == null) {
      throw new IllegalArgumentException("Robot status cannot be null");
    }
    return robotTask;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private RobotTask robotTask;
    private RobotStatus robotStatus;
    private String color;
    private Instant timestamp;
    private Map<String, String> metadata;

    public Builder robotTask(RobotTask robotTask) {
      this.robotTask = robotTask;
      return this;
    }

    public Builder robotStatus(RobotStatus robotStatus) {
      this.robotStatus = robotStatus;
      return this;
    }

    public Builder color(String color) {
      this.color = color;
      return this;
    }

    public Builder timestamp(Instant timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder metadata(Map<String, String> metadata) {
      this.metadata = metadata;
      return this;
    }

    public RobotData build() {
      return new RobotData(robotTask, robotStatus, color, timestamp, metadata);
    }
  }
}
