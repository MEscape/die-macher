package com.die_macher.application.service;

import com.die_macher.domain.model.robot.RobotData;
import com.die_macher.domain.port.inbound.HistoricalRobotDataProvider;
import com.die_macher.domain.port.outbound.RobotDataRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class HistoricalRobotDataService implements HistoricalRobotDataProvider {
  private final RobotDataRepository robotDataRepository;

  public HistoricalRobotDataService(RobotDataRepository robotDataRepository) {
    this.robotDataRepository = robotDataRepository;
  }

  @Override
  @Cacheable(value = "historicalRobotData", key = "#start + '_' + #end")
  public CompletableFuture<List<RobotData>> getHistoricalData(Instant start, Instant end) {
    validateTimeRange(start, end);

    return robotDataRepository.findByTimeRange(start, end);
  }

  private void validateTimeRange(Instant start, Instant end) {
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start time must be before end time");
    }
    if (Duration.between(start, end).toDays() > 365) {
      throw new IllegalArgumentException("Time range cannot exceed 365 days");
    }
  }
}
