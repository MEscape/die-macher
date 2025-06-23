package com.die_macher.domain.port.outbound;

import com.die_macher.domain.model.robot.RobotData;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RobotDataRepository {
  CompletableFuture<Void> save(RobotData robotData);

  CompletableFuture<List<RobotData>> findByTimeRange(Instant start, Instant end);
}
