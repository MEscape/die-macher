package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.robot.RobotData;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HistoricalRobotDataProvider {
  CompletableFuture<List<RobotData>> getHistoricalData(Instant start, Instant end);
}
