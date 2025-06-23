package com.die_macher.domain.port.inbound;

import com.die_macher.infrastructure.adapter.web.dto.CubeManipulationRequest;

public interface TcpMonitoringClientProvider {
  void sendCubeProcessedEvent(int cubeCount);

  void sendCubeManipulation(String color, CubeManipulationRequest.Action action);
}
