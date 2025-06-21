package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.SensorData;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoricalDataProvider {
    CompletableFuture<List<SensorData>> getHistoricalData(String sensorId,
                                                          Instant start,
                                                          Instant end);

    CompletableFuture<SensorData> getAggregatedData(String sensorId,
                                                    Instant start,
                                                    Instant end,
                                                    String interval);
}
