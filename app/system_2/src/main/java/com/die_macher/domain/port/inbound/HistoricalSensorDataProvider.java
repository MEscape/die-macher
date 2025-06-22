package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.model.SensorType;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoricalSensorDataProvider {
  CompletableFuture<List<SensorData>> getHistoricalData(
      String sensorId, Instant start, Instant end);

  CompletableFuture<SensorData> getAggregatedData(
      String sensorId, SensorType sensorType, Instant start, Instant end, String interval);
}
