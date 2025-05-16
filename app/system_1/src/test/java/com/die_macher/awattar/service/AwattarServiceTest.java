package com.die_macher.awattar.service;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AwattarServiceTest {

    private RestTemplate restTemplate;
    private AwattarService awattarService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        awattarService = new AwattarService(restTemplate);
    }

    @Test
    void testFetchMarketDataFor_returnsData() {
        MarketData marketData = new MarketData();
        when(restTemplate.getForEntity(anyString(), eq(MarketData.class)))
                .thenReturn(ResponseEntity.ok(marketData));

        MarketData result = awattarService.fetchMarketDataFor(123L);
        assertNotNull(result);
    }

    @Test
    void testFetchMarketDataFor_returnsNullOnException() {
        when(restTemplate.getForEntity(anyString(), eq(MarketData.class)))
                .thenThrow(new RuntimeException("API error"));

        MarketData result = awattarService.fetchMarketDataFor(123L);
        assertNull(result);
    }

    @Test
    void testFetchCurrentMarketData_callsFetchMarketDataFor() {
        AwattarService spyService = spy(awattarService);
        doReturn(new MarketData()).when(spyService).fetchMarketDataFor(anyLong());

        MarketData result = spyService.fetchCurrentMarketData();
        assertNotNull(result);
        verify(spyService, times(1)).fetchMarketDataFor(anyLong());
    }

    @Test
    void testFetchTomorrowMarketData_callsFetchMarketDataFor() {
        AwattarService spyService = spy(awattarService);
        doReturn(new MarketData()).when(spyService).fetchMarketDataFor(anyLong());

        MarketData result = spyService.fetchTomorrowMarketData();
        assertNotNull(result);
        verify(spyService, times(1)).fetchMarketDataFor(anyLong());
    }

    @Test
    void testCalculateOptimalProductionWindow_findsBestWindow() {
        // Prepare 5 hours of prices: 100, 90, 80, 70, 60 (lowest 3 are 80,70,60)
        MarketPrice p1 = new MarketPrice(); p1.setMarketprice(100); p1.setStartTimestamp(1); p1.setEnd_timestamp(2);
        MarketPrice p2 = new MarketPrice(); p2.setMarketprice(90);  p2.setStartTimestamp(2); p2.setEnd_timestamp(3);
        MarketPrice p3 = new MarketPrice(); p3.setMarketprice(80);  p3.setStartTimestamp(3); p3.setEnd_timestamp(4);
        MarketPrice p4 = new MarketPrice(); p4.setMarketprice(70);  p4.setStartTimestamp(4); p4.setEnd_timestamp(5);
        MarketPrice p5 = new MarketPrice(); p5.setMarketprice(60);  p5.setStartTimestamp(5); p5.setEnd_timestamp(6);

        MarketData marketData = new MarketData();
        marketData.setData(Arrays.asList(p1, p2, p3, p4, p5));

        AwattarService service = new AwattarService(restTemplate);
        // Inject test data
        service.cachedMarketData = marketData;
        // Call private method via reflection
        try {
            var method = AwattarService.class.getDeclaredMethod("calculateOptimalProductionWindow");
            method.setAccessible(true);
            method.invoke(service);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }

        OptimalProductionWindow window = service.getOptimalProductionWindow();
        assertNotNull(window);
        assertEquals(3, window.getPrices().size());
        // The best window should start at p3 (80,70,60)
        assertEquals(3, window.getPrices().get(0).getStart_timestamp());
        assertEquals(60, window.getPrices().get(2).getMarketprice());
    }

    @Test
    void testCalculateProductionCost_returnsZeroForEmptyList() {
        List<MarketPrice> emptyList = Collections.emptyList();
        double cost = invokeCalculateProductionCost(awattarService, emptyList);
        assertEquals(0.0, cost);
    }

    // Helper to call private method calculateProductionCost
    private double invokeCalculateProductionCost(AwattarService service, List<MarketPrice> prices) {
        try {
            var method = AwattarService.class.getDeclaredMethod("calculateProductionCost", List.class);
            method.setAccessible(true);
            return (double) method.invoke(service, prices);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
            return -1;
        }
    }
}