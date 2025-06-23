package com.die_macher.application.service;

import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.price.PriceData;
import com.die_macher.domain.port.inbound.PriceDataProcessor;
import com.die_macher.domain.port.outbound.MessagePublisher;
import com.die_macher.domain.port.outbound.PriceDataRepository;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PriceDataService implements PriceDataProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(PriceDataService.class);
  private final PriceDataRepository priceDataRepository;
  private final MessagePublisher messagePublisher;

  public PriceDataService(
      PriceDataRepository priceDataRepository, MessagePublisher messagePublisher) {
    this.priceDataRepository = priceDataRepository;
    this.messagePublisher = messagePublisher;
  }

  @Override
  public CompletableFuture<Void> storePriceData(PriceData priceData) {
    return priceDataRepository
        .save(priceData)
        .exceptionally(
            throwable -> {
              LOGGER.error("Failed to store price data", throwable);
              throw new DataProcessingException("Storage failed", throwable);
            });
  }

  @Override
  public CompletableFuture<Void> publishToMqtt(PriceData priceData) {
    return messagePublisher
        .publish(priceData)
        .exceptionally(
            throwable -> {
              LOGGER.error("Failed to publish price data to MQTT", throwable);
              throw new DataProcessingException("MQTT publish failed", throwable);
            });
  }
}
