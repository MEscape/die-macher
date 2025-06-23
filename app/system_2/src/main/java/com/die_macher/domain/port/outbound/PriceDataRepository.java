package com.die_macher.domain.port.outbound;

import com.die_macher.domain.model.price.PriceData;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PriceDataRepository {
  CompletableFuture<Void> save(PriceData priceData);

  CompletableFuture<List<PriceData>> findByTimeRange(Instant start, Instant end);

  CompletableFuture<PriceData> aggregatePriceData(
      String field, Instant start, Instant end, String interval);
}
