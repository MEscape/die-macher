package com.die_macher.awattar.controller;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import com.die_macher.awattar.service.AwattarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AwattarController.class)
class AwattarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AwattarService awattarService;

    AwattarControllerTest(AwattarService awattarService) {
        this.awattarService = awattarService;
    }

    @Test
    void testGetMarketData_returnsOk() throws Exception {
        MarketData marketData = new MarketData();
        marketData.setObject("list");
        marketData.setData(Collections.emptyList());
        when(awattarService.fetchTomorrowMarketData()).thenReturn(marketData);

        mockMvc.perform(get("/api/awattar/tomorrow-prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("list"));
    }

    @Test
    void testGetMarketData_returnsNotFound() throws Exception {
        when(awattarService.fetchTomorrowMarketData()).thenReturn(null);

        mockMvc.perform(get("/api/awattar/tomorrow-prices"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetCurrentMarketData_returnsOk() throws Exception {
        MarketPrice price = new MarketPrice();
        price.setStart_timestamp(123L);
        price.setEnd_timestamp(456L);
        price.setMarketprice(100.0);
        price.setUnit("EUR/MWh");

        MarketData marketData = new MarketData();
        marketData.setData(List.of(price));
        when(awattarService.fetchCurrentMarketData()).thenReturn(marketData);

        mockMvc.perform(get("/api/awattar/current-price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marketprice").value(100.0));
    }

    @Test
    void testGetCurrentMarketData_returnsNotFound() throws Exception {
        when(awattarService.fetchCurrentMarketData()).thenReturn(null);

        mockMvc.perform(get("/api/awattar/current-price"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOptimalProductionWindow_returnsOk() throws Exception {
        OptimalProductionWindow window = new OptimalProductionWindow(
                123L, 456L, Collections.emptyList(), 12.34
        );
        when(awattarService.getOptimalProductionWindow()).thenReturn(window);

        mockMvc.perform(get("/api/awattar/optimal-window"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(12.34));
    }

    @Test
    void testGetOptimalProductionWindow_returnsNotFound() throws Exception {
        when(awattarService.getOptimalProductionWindow()).thenReturn(null);

        mockMvc.perform(get("/api/awattar/optimal-window"))
                .andExpect(status().isNotFound());
    }
}