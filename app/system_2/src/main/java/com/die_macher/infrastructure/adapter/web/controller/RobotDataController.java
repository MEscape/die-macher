package com.die_macher.infrastructure.adapter.web.controller;

import com.die_macher.domain.port.inbound.HistoricalRobotDataProvider;
import com.die_macher.domain.port.inbound.TcpMonitoringClientProvider;
import com.die_macher.infrastructure.adapter.web.dto.CubeManipulationRequest;
import com.die_macher.infrastructure.adapter.web.dto.RobotDataResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/robot-data")
@Validated
public class RobotDataController {

  private final HistoricalRobotDataProvider historicalRobotDataProvider;
  private final TcpMonitoringClientProvider tcpMonitoringClientProvider;

  public RobotDataController(
      HistoricalRobotDataProvider historicalRobotDataProvider,
      TcpMonitoringClientProvider tcpMonitoringClientProvider) {
    this.historicalRobotDataProvider = historicalRobotDataProvider;
    this.tcpMonitoringClientProvider = tcpMonitoringClientProvider;
  }

  @GetMapping
  public CompletableFuture<ResponseEntity<List<RobotDataResponse>>> getHistoricalRobotData(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

    return historicalRobotDataProvider
        .getHistoricalData(start, end)
        .thenApply(data -> ResponseEntity.ok(data.stream().map(RobotDataResponse::from).toList()));
  }

  @PostMapping
  public CompletableFuture<ResponseEntity<Void>> postHistoricalRobotData(
      @RequestParam @Min(1) @Max(5) int cubeCount) {

    return CompletableFuture.runAsync(
            () -> tcpMonitoringClientProvider.sendCubeProcessedEvent(cubeCount))
        .thenApply(voidResult -> ResponseEntity.accepted().build());
  }

  @PostMapping("/cube")
  public CompletableFuture<ResponseEntity<Void>> manipulateCube(
      @RequestBody @Valid CubeManipulationRequest request) {

    return CompletableFuture.runAsync(
            () ->
                tcpMonitoringClientProvider.sendCubeManipulation(
                    request.getColor(), request.getAction()))
        .thenApply(voidResult -> ResponseEntity.accepted().build());
  }
}
