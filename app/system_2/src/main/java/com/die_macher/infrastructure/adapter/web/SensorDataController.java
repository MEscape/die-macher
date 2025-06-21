package com.die_macher.infrastructure.adapter.web;

import com.die_macher.application.service.HistoricalDataService;
import com.die_macher.infrastructure.adapter.web.dto.SensorDataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sensor-data")
@Validated
public class SensorDataController {

    private final HistoricalDataService historicalDataService;

    public SensorDataController(HistoricalDataService historicalDataService) {
        this.historicalDataService = historicalDataService;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<SensorDataResponse>>> getHistoricalData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(required = false) String sensorId) {

        return historicalDataService.getHistoricalData(sensorId, start, end)
                .thenApply(data -> ResponseEntity.ok(
                        data.stream()
                                .map(SensorDataResponse::from)
                                .toList()
                ));
    }

    @GetMapping("/{sensorId}")
    public CompletableFuture<ResponseEntity<List<SensorDataResponse>>> getSensorData(
            @PathVariable String sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        return historicalDataService.getHistoricalData(sensorId, start, end)
                .thenApply(data -> ResponseEntity.ok(
                        data.stream()
                                .map(SensorDataResponse::from)
                                .toList()
                ));
    }

    @GetMapping("/aggregated")
    public CompletableFuture<ResponseEntity<SensorDataResponse>> getAggregatedData(
            @RequestParam String sensorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "1h") String interval) {

        return historicalDataService.getAggregatedData(sensorId, start, end, interval)
                .thenApply(data -> ResponseEntity.ok(SensorDataResponse.from(data)));
    }
}
