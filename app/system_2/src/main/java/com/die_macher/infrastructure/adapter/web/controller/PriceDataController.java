package com.die_macher.infrastructure.adapter.web.controller;

import com.die_macher.application.service.HistoricalPriceDataService;
import com.die_macher.infrastructure.adapter.web.dto.PriceDataResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/price-data")
@Validated
public class PriceDataController {

    private final HistoricalPriceDataService historicalPriceDataService;

    public PriceDataController(HistoricalPriceDataService historicalPriceDataService) {
        this.historicalPriceDataService = historicalPriceDataService;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<PriceDataResponse>>> getHistoricalPriceData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {

        return historicalPriceDataService.getHistoricalData(start, end)
                .thenApply(data -> ResponseEntity.ok(
                        data.stream()
                                .map(PriceDataResponse::from)
                                .toList()
                ));
    }

    @GetMapping("/aggregated")
    public CompletableFuture<ResponseEntity<PriceDataResponse>> getAggregatedPriceData(
            @RequestParam String field,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end,
            @RequestParam(defaultValue = "1h") String interval) {

        return historicalPriceDataService.getAggregatedData(field, start, end, interval)
                .thenApply(data -> ResponseEntity.ok(PriceDataResponse.from(data)));
    }
}
