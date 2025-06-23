package com.die_macher.application.service;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.sensor.SensorData;
import com.die_macher.domain.model.sensor.SensorType;
import com.die_macher.domain.port.inbound.SensorDataProcessor;
import com.die_macher.domain.port.outbound.MessagePublisher;
import com.die_macher.domain.port.outbound.SensorDataRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SensorDataService implements SensorDataProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(SensorDataService.class);
  private final SensorDataRepository sensorDataRepository;
  private final MessagePublisher messagePublisher;

  public SensorDataService(
      SensorDataRepository sensorDataRepository, MessagePublisher messagePublisher) {
    this.sensorDataRepository = sensorDataRepository;
    this.messagePublisher = messagePublisher;
  }

  @Override
  public CompletableFuture<Void> storeSensorData(SensorData sensorData) {
    validateSensorData(sensorData);

    return sensorDataRepository
        .save(sensorData)
        .exceptionally(
            throwable -> {
              LOGGER.error("Failed to store sensor data: {}", sensorData.sensorId(), throwable);
              throw new DataProcessingException("Storage failed", throwable);
            });
  }

  @Override
  public CompletableFuture<Void> publishToMqtt(SensorData sensorData) {
    validateSensorData(sensorData);

    return messagePublisher
        .publish(sensorData)
        .exceptionally(
            throwable -> {
              LOGGER.error(
                  "Failed to publish sensor data to MQTT: {}", sensorData.sensorId(), throwable);
              throw new DataProcessingException("MQTT publish failed", throwable);
            });
  }

  @Transactional
  public CompletableFuture<Void> processBatch(List<SensorData> sensorDataList) {
    return sensorDataRepository
        .saveBatch(sensorDataList)
        .thenCompose(
            v -> {
              List<CompletableFuture<Void>> publishFutures =
                  sensorDataList.stream().map(this::publishToMqtt).toList();
              return CompletableFuture.allOf(publishFutures.toArray(new CompletableFuture[0]));
            });
  }

  private void validateSensorData(SensorData sensorData) {
    if (sensorData.value() == null) {
      throw new IllegalArgumentException("Sensor value cannot be null");
    }
    if (sensorData.value() < -273.15 && sensorData.type() == SensorType.TEMPERATURE) {
      throw new IllegalArgumentException("Temperature cannot be below absolute zero");
    }
  }
}
