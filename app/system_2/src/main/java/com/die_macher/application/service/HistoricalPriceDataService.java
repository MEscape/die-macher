package com.die_macher.application.service;

import com.die_macher.domain.model.PriceData;
import com.die_macher.domain.port.inbound.HistoricalPriceDataProvider;
import com.die_macher.domain.port.outbound.PriceDataRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class HistoricalPriceDataService implements HistoricalPriceDataProvider {

    private final PriceDataRepository priceDataRepository;

    public HistoricalPriceDataService(PriceDataRepository priceDataRepository) {
        this.priceDataRepository = priceDataRepository;
    }

    @Override
    @Cacheable(value = "historicalPriceData", key = "#start + '_' + #end")
    public CompletableFuture<List<PriceData>> getHistoricalData(Instant start,
                                                                 Instant end) {
        validateTimeRange(start, end);

        return priceDataRepository.findByTimeRange(start, end);
    }

    @Override
    @Cacheable(value = "aggregatedPriceData", key = "#field + '_' + #start + '_' + #end + '_' + #interval")
    public CompletableFuture<PriceData> getAggregatedData(
                                                          String field,
                                                          Instant start,
                                                          Instant end,
                                                          String interval) {
        validateTimeRange(start, end);

        return priceDataRepository.aggregatePriceData(field, start, end, interval);
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
