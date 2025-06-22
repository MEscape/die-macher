package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.PriceData;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoricalPriceDataProvider {

  CompletableFuture<List<PriceData>> getHistoricalData(Instant start, Instant end);

  CompletableFuture<PriceData> getAggregatedData(
      String field, Instant start, Instant end, String interval);
}
