package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.SensorData;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SensorDataProcessor {
    CompletableFuture<Void> processSensorData(SensorData sensorData);
    CompletableFuture<Void> storeSensorData(SensorData sensorData);
    CompletableFuture<Void> publishToMqtt(SensorData sensorData);
    CompletableFuture<Void> processBatch(List<SensorData> sensorDataList);
}
