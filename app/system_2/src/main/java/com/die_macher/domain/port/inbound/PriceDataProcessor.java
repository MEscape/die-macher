package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.price.PriceData;
import java.util.concurrent.CompletableFuture;

public interface PriceDataProcessor {
  CompletableFuture<Void> storePriceData(PriceData priceData);

  CompletableFuture<Void> publishToMqtt(PriceData priceData);
}
