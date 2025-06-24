package com.die_macher.domain.port.inbound;

import com.die_macher.domain.model.price.PriceData;
import com.die_macher.infrastructure.adapter.web.dto.CubeManipulationRequest;
import com.die_macher.infrastructure.adapter.web.dto.PriceDataResponse;
import com.die_macher.infrastructure.adapter.web.dto.TomorrowDataRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TcpMonitoringClientProvider {
  void sendCubeProcessedEvent(int cubeCount);

  void sendCubeManipulation(String color, CubeManipulationRequest.Action action);

  CompletableFuture<List<PriceData>> sendAwattarDataRequest(TomorrowDataRequest.Which which);
}
