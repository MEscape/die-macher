package com.die_macher.awattar.controller;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.service.AwattarService;
import com.die_macher.awattar.model.OptimalProductionWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/awattar")  
public class AwattarController {

    private final AwattarService awattarService;

    @Autowired
    public AwattarController(AwattarService awattarService) {
        this.awattarService = awattarService;
    }

    @GetMapping("/tomorrow-prices")
    public ResponseEntity<MarketData> getMarketData() {
        MarketData data = awattarService.fetchTomorrowMarketData();
        if (data != null) {
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/current-price")
    public ResponseEntity<MarketPrice> getCurrentMarketData() {
        MarketData marketData = awattarService.fetchCurrentMarketData();
        if (marketData != null) {
            MarketPrice data = marketData.getFirstPrice();
            if (data != null) {
                return ResponseEntity.ok(data);
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/optimal-window")
    public ResponseEntity<OptimalProductionWindow> getOptimalProductionWindow() {
        OptimalProductionWindow window = awattarService.getOptimalProductionWindow();
        if (window != null) {
            return ResponseEntity.ok(window);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}