package com.die_macher.domain.port.outbound;

import java.util.concurrent.CompletableFuture;

public interface MessagePublisher {
  CompletableFuture<Void> publish(Object payload);
}
