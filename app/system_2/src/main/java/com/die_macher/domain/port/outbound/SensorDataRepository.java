package com.die_macher.domain.port.outbound;

import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.model.SensorType;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SensorDataRepository {
    CompletableFuture<Void> save(SensorData sensorData);
    CompletableFuture<List<SensorData>> findBySensorIdAndTimeRange(
            String sensorId, Instant start, Instant end);
    CompletableFuture<List<SensorData>> findByTimeRange(Instant start, Instant end);
    CompletableFuture<Void> saveBatch(List<SensorData> sensorDataList);
    CompletableFuture<SensorData> aggregateSensorData(
            String sensorId, SensorType sensorType, Instant start, Instant end, String interval);
}
