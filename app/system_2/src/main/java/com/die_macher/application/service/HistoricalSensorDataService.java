package com.die_macher.application.service;

import com.die_macher.domain.model.sensor.SensorData;
import com.die_macher.domain.model.sensor.SensorType;
import com.die_macher.domain.port.inbound.HistoricalSensorDataProvider;
import com.die_macher.domain.port.outbound.SensorDataRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class HistoricalSensorDataService implements HistoricalSensorDataProvider {

  private final SensorDataRepository sensorDataRepository;

  public HistoricalSensorDataService(SensorDataRepository sensorDataRepository) {
    this.sensorDataRepository = sensorDataRepository;
  }

  @Override
  @Cacheable(value = "historicalSensorData", key = "#sensorId + '_' + #start + '_' + #end")
  public CompletableFuture<List<SensorData>> getHistoricalData(
      String sensorId, Instant start, Instant end) {
    validateTimeRange(start, end);

    if (sensorId != null && !sensorId.trim().isEmpty()) {
      return sensorDataRepository.findBySensorIdAndTimeRange(sensorId, start, end);
    } else {
      return sensorDataRepository.findByTimeRange(start, end);
    }
  }

  @Override
  @Cacheable(
      value = "aggregatedSensorData",
      key = "#sensorId + '_' + #sensorType + '_' + #start + '_' + #end + '_' + #interval")
  public CompletableFuture<SensorData> getAggregatedData(
      String sensorId, SensorType sensorType, Instant start, Instant end, String interval) {
    validateTimeRange(start, end);

    return sensorDataRepository.aggregateSensorData(sensorId, sensorType, start, end, interval);
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
