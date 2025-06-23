package com.die_macher.infrastructure.adapter.web.controller;

import com.die_macher.domain.port.inbound.HistoricalPriceDataProvider;
import com.die_macher.domain.port.inbound.TcpMonitoringClientProvider;
import com.die_macher.infrastructure.adapter.web.dto.PriceDataResponse;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.die_macher.infrastructure.adapter.web.dto.TomorrowDataRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/price-data")
@Validated
public class PriceDataController {

  private final HistoricalPriceDataProvider historicalPriceDataProvider;
  private final TcpMonitoringClientProvider  tcpMonitoringClientProvider;

  public PriceDataController(HistoricalPriceDataProvider historicalPriceDataProvider, TcpMonitoringClientProvider tcpMonitoringClientProvider) {
    this.historicalPriceDataProvider = historicalPriceDataProvider;
      this.tcpMonitoringClientProvider = tcpMonitoringClientProvider;
  }

  @GetMapping
  public CompletableFuture<ResponseEntity<List<PriceDataResponse>>> getHistoricalPriceData(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

    return historicalPriceDataProvider
        .getHistoricalData(start, end)
        .thenApply(data -> ResponseEntity.ok(data.stream().map(PriceDataResponse::from).toList()));
  }

  @GetMapping("/aggregated")
  public CompletableFuture<ResponseEntity<PriceDataResponse>> getAggregatedPriceData(
      @RequestParam String field,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
      @RequestParam(defaultValue = "1h") String interval) {

    return historicalPriceDataProvider
        .getAggregatedData(field, start, end, interval)
        .thenApply(data -> ResponseEntity.ok(PriceDataResponse.from(data)));
  }

  @GetMapping("/tomorrow")
  public CompletableFuture<ResponseEntity<List<PriceDataResponse>>> getTomorrowPriceData(
          @RequestParam TomorrowDataRequest.Which which) {
    return tcpMonitoringClientProvider.sendAwattarDataRequest(which)
            .thenApply(data -> ResponseEntity.ok(PriceDataResponse.fromList(data)));
  }

}
