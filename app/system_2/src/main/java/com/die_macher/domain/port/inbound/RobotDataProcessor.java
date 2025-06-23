package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.robot.RobotData;
import java.util.concurrent.CompletableFuture;

public interface RobotDataProcessor {
  CompletableFuture<Void> storeRobotData(RobotData robotData);

  CompletableFuture<Void> publishToMqtt(RobotData robotData);
}
