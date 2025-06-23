package com.die_macher.infrastructure.adapter.web.controller;

import com.die_macher.domain.model.sensor.SensorType;
import com.die_macher.domain.port.inbound.HistoricalSensorDataProvider;
import com.die_macher.infrastructure.adapter.web.dto.SensorDataResponse;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensor-data")
@Validated
public class SensorDataController {

  private final HistoricalSensorDataProvider historicalSensorDataProvider;

  public SensorDataController(HistoricalSensorDataProvider historicalSensorDataProvider) {
    this.historicalSensorDataProvider = historicalSensorDataProvider;
  }

  @GetMapping
  public CompletableFuture<ResponseEntity<List<SensorDataResponse>>> getHistoricalData(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

    return historicalSensorDataProvider
        .getHistoricalData(null, start, end)
        .thenApply(data -> ResponseEntity.ok(data.stream().map(SensorDataResponse::from).toList()));
  }

  @GetMapping("/{sensorId}")
  public CompletableFuture<ResponseEntity<List<SensorDataResponse>>> getSensorData(
      @PathVariable String sensorId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

    return historicalSensorDataProvider
        .getHistoricalData(sensorId, start, end)
        .thenApply(data -> ResponseEntity.ok(data.stream().map(SensorDataResponse::from).toList()));
  }

  @GetMapping("/aggregated")
  public CompletableFuture<ResponseEntity<SensorDataResponse>> getAggregatedData(
      @RequestParam String sensorId,
      @RequestParam SensorType sensorType,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
      @RequestParam(defaultValue = "1h") String interval) {

    return historicalSensorDataProvider
        .getAggregatedData(sensorId, sensorType, start, end, interval)
        .thenApply(data -> ResponseEntity.ok(SensorDataResponse.from(data)));
  }
}
