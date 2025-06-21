package com.die_macher.domain.port.outbound;

import com.die_macher.domain.model.SensorData;

import java.util.concurrent.CompletableFuture;

public interface MessagePublisher {
    CompletableFuture<Void> publish(SensorData sensorData);
}
