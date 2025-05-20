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

/**
 * REST controller for handling Awattar electricity market data requests.
 * This controller provides endpoints to access electricity market prices
 * and optimal production windows based on price data.
 */
@RestController
@RequestMapping("/api/awattar")  
public class AwattarController {

    private final AwattarService awattarService;

    /**
     * Constructs an AwattarController with the required service dependency.
     *
     * @param awattarService The service used to fetch and process market data
     */
    @Autowired
    public AwattarController(AwattarService awattarService) {
        this.awattarService = awattarService;
    }

    /**
     * Retrieves electricity market price data for tomorrow.
     *
     * @return ResponseEntity containing market data if available, or a 404 Not Found response
     */
    @GetMapping("/tomorrow-prices")
    public ResponseEntity<MarketData> getMarketData() {
        MarketData data = awattarService.fetchTomorrowMarketData();
        if (data != null) {
            return ResponseEntity.ok(data);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Retrieves the current electricity market price.
     *
     * @return ResponseEntity containing the current market price if available, or a 404 Not Found response
     */
    @GetMapping("/current-price")
    public ResponseEntity<MarketPrice> getCurrentMarketData() {
        MarketData marketData = awattarService.fetchCurrentMarketData();
        if (marketData != null) {
            MarketPrice data = marketData.getCurrentPrice();
            if (data != null) {
                return ResponseEntity.ok(data);
            }
        }
        return ResponseEntity.notFound().build();
    }
    
    /**
     * Retrieves the optimal time window for electricity production based on market prices.
     *
     * @return ResponseEntity containing the optimal production window if available, or a 404 Not Found response
     */
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