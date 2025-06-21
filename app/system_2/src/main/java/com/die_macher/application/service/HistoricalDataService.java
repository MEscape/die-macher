package com.die_macher.application.service;

import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.port.inbound.HistoricalDataProvider;
import com.die_macher.domain.port.outbound.SensorDataRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class HistoricalDataService implements HistoricalDataProvider {

    private final SensorDataRepository sensorDataRepository;

    public HistoricalDataService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @Override
    @Cacheable(value = "historicalData", key = "#sensorId + '_' + #start + '_' + #end")
    public CompletableFuture<List<SensorData>> getHistoricalData(String sensorId,
                                                                 Instant start,
                                                                 Instant end) {
        validateTimeRange(start, end);

        if (sensorId != null && !sensorId.trim().isEmpty()) {
            return sensorDataRepository.findBySensorIdAndTimeRange(sensorId, start, end);
        } else {
            return sensorDataRepository.findByTimeRange(start, end);
        }
    }

    @Override
    @Cacheable(value = "aggregatedData", key = "#sensorId + '_' + #start + '_' + #end + '_' + #interval")
    public CompletableFuture<SensorData> getAggregatedData(String sensorId,
                                                                       Instant start,
                                                                       Instant end,
                                                                       String interval) {
        validateTimeRange(start, end);

        return sensorDataRepository.aggregateSensorData(sensorId, start, end, interval);
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
