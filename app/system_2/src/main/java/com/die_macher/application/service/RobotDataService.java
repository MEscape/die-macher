package com.die_macher.application.service;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.robot.RobotData;
import com.die_macher.domain.port.inbound.RobotDataProcessor;
import com.die_macher.domain.port.outbound.MessagePublisher;
import com.die_macher.domain.port.outbound.RobotDataRepository;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RobotDataService implements RobotDataProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(RobotDataService.class);
  private final RobotDataRepository robotDataRepository;
  private final MessagePublisher messagePublisher;

  public RobotDataService(
      RobotDataRepository robotDataRepository, MessagePublisher messagePublisher) {
    this.robotDataRepository = robotDataRepository;
    this.messagePublisher = messagePublisher;
  }

  @Override
  public CompletableFuture<Void> storeRobotData(RobotData robotData) {
    return robotDataRepository
        .save(robotData)
        .exceptionally(
            throwable -> {
              LOGGER.error("Failed to store robot data", throwable);
              throw new DataProcessingException("Storage failed", throwable);
            });
  }

  @Override
  public CompletableFuture<Void> publishToMqtt(RobotData robotData) {
    return messagePublisher
        .publish(robotData)
        .exceptionally(
            throwable -> {
              LOGGER.error("Failed to publish robot data to MQTT", throwable);
              throw new DataProcessingException("MQTT publish failed", throwable);
            });
  }
}
