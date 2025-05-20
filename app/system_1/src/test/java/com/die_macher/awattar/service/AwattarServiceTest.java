package com.die_macher.awattar.service;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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
        MarketPrice p1 = new MarketPrice(); p1.setMarketprice(100); p1.setStart_timestamp(1); p1.setEnd_timestamp(2);
        MarketPrice p2 = new MarketPrice(); p2.setMarketprice(90);  p2.setStart_timestamp(2); p2.setEnd_timestamp(3);
        MarketPrice p3 = new MarketPrice(); p3.setMarketprice(80);  p3.setStart_timestamp(3); p3.setEnd_timestamp(4);
        MarketPrice p4 = new MarketPrice(); p4.setMarketprice(70);  p4.setStart_timestamp(4); p4.setEnd_timestamp(5);
        MarketPrice p5 = new MarketPrice(); p5.setMarketprice(60);  p5.setStart_timestamp(5); p5.setEnd_timestamp(6);

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
    
    @Test
    void testCalculateProductionCost_calculatesCorrectly() {
        // Create test data with known prices
        MarketPrice p1 = mock(MarketPrice.class);
        when(p1.getMarketprice()).thenReturn(100.0); // EUR/MWh
        when(p1.getPriceInEurPerKwh()).thenReturn(0.1); // 100 EUR/MWh = 0.1 EUR/kWh
    
        MarketPrice p2 = mock(MarketPrice.class);
        when(p2.getMarketprice()).thenReturn(200.0); // EUR/MWh
        when(p2.getPriceInEurPerKwh()).thenReturn(0.2); // 200 EUR/MWh = 0.2 EUR/kWh
    
        MarketPrice p3 = mock(MarketPrice.class);
        when(p3.getMarketprice()).thenReturn(300.0); // EUR/MWh
        when(p3.getPriceInEurPerKwh()).thenReturn(0.3); // 300 EUR/MWh = 0.3 EUR/kWh
    
        List<MarketPrice> prices = Arrays.asList(p1, p2, p3);
    
        // Expected result calculation:
        // ENERGY_PER_PART = 0.2 kWh, PARTS_PER_HOUR = 5, PRODUCTION_HOURS = 3
        // Average price = (0.1 + 0.2 + 0.3) / 3 = 0.2 EUR/kWh
        // Total energy = 0.2 kWh * 5 parts/hour * 3 hours = 3 kWh
        // Total cost = 0.2 EUR/kWh * 3 kWh = 0.6 EUR
        double expectedCost = 0.6;
    
        double actualCost = invokeCalculateProductionCost(awattarService, prices);
        assertEquals(expectedCost, actualCost, 0.001);
    }
    
    @Test
    void testGetOptimalProductionWindow_calculatesWhenNull() {
        // Vorbereitung: optimalWindow ist null, fetchTomorrowMarketData wird aufgerufen
        AwattarService spyService = spy(awattarService);
        
        // Mock für fetchTomorrowMarketData
        MarketData marketData = new MarketData();
        List<MarketPrice> prices = createTestPrices();
        marketData.setData(prices);
        doReturn(marketData).when(spyService).fetchTomorrowMarketData();
        
        // Aufruf der zu testenden Methode
        OptimalProductionWindow result = spyService.getOptimalProductionWindow();
        
        // Überprüfungen
        assertNotNull(result);
        verify(spyService).fetchTomorrowMarketData();
    }
    
    @Test
    void testGetOptimalProductionWindow_returnsExistingWindow() {
        // Vorbereitung: optimalWindow ist bereits vorhanden
        OptimalProductionWindow existingWindow = new OptimalProductionWindow(1L, 2L, Collections.emptyList(), 10.0);
        
        AwattarService spyService = spy(awattarService);
        // Setze das Feld direkt über Reflection
        try {
            var field = AwattarService.class.getDeclaredField("optimalWindow");
            field.setAccessible(true);
            field.set(spyService, existingWindow);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
        
        // Aufruf der zu testenden Methode
        OptimalProductionWindow result = spyService.getOptimalProductionWindow();
        
        // Überprüfungen
        assertSame(existingWindow, result);
        verify(spyService, never()).fetchTomorrowMarketData();
    }
    
    @Test
    void testGetCurrentPartCost_returnsCorrectCost() {
        // Preparation
        AwattarService spyService = spy(awattarService);
    
        // Use a mock for MarketData
        MarketData marketData = mock(MarketData.class);
        MarketPrice currentPrice = mock(MarketPrice.class);
        when(currentPrice.getPriceInEurPerKwh()).thenReturn(0.25); // 0.25 EUR/kWh
    
        // Set currentPrice as the current price
        doReturn(marketData).when(spyService).fetchCurrentMarketData();
        when(marketData.getCurrentPrice()).thenReturn(currentPrice);
    
        // Expected result: ENERGY_PER_PART (0.2 kWh) * price per kWh (0.25 EUR/kWh) = 0.05 EUR
        double expectedCost = 0.05;
    
        // Call the method under test
        double result = spyService.getCurrentPartCost();
    
        // Assertions
        assertEquals(expectedCost, result, 0.001);
        verify(spyService).fetchCurrentMarketData();
    }
    
    @Test
    void testGetCurrentPartCost_returnsNegativeOneWhenNoData() {
        // Vorbereitung: fetchCurrentMarketData gibt null zurück
        AwattarService spyService = spy(awattarService);
        doReturn(null).when(spyService).fetchCurrentMarketData();
        
        // Aufruf der zu testenden Methode
        double result = spyService.getCurrentPartCost();
        
        // Überprüfungen
        assertEquals(-1, result);
    }
    
    @Test
    void testGetCurrentPartCost_returnsNegativeOneWhenNoCurrentPrice() {
        // Preparation: getCurrentPrice returns null
        AwattarService spyService = spy(awattarService);
    
        // Mock MarketData instead of using a real instance
        MarketData marketData = mock(MarketData.class);
        doReturn(marketData).when(spyService).fetchCurrentMarketData();
        when(marketData.getCurrentPrice()).thenReturn(null);
    
        // Call the method under test
        double result = spyService.getCurrentPartCost();
    
        // Assertions
        assertEquals(-1, result);
    }
    
    @Test
    void testUpdateMarketData() {
        // Vorbereitung
        AwattarService spyService = spy(awattarService);
        MarketData marketData = new MarketData();
        List<MarketPrice> prices = createTestPrices();
        marketData.setData(prices);
        
        doReturn(marketData).when(spyService).fetchTomorrowMarketData();
        
        // Aufruf der zu testenden Methode
        spyService.updateMarketData();
        
        // Überprüfungen
        verify(spyService).fetchTomorrowMarketData();
        assertSame(marketData, spyService.cachedMarketData);
    }

    @Test
    void testCalculateOptimalProductionWindow_insufficientData() {
        // Vorbereitung: Nicht genügend Daten für die Berechnung
        AwattarService service = new AwattarService(restTemplate);
        
        // Fall 1: cachedMarketData ist null
        service.cachedMarketData = null;
        invokeCalculateOptimalProductionWindow(service);
        assertNull(service.getOptimalProductionWindow());
        
        // Fall 2: cachedMarketData.getData() ist null
        MarketData marketData = new MarketData();
        marketData.setData(null);
        service.cachedMarketData = marketData;
        invokeCalculateOptimalProductionWindow(service);
        assertNull(service.getOptimalProductionWindow());
        
        // Fall 3: cachedMarketData.getData() hat weniger als PRODUCTION_HOURS Einträge
        List<MarketPrice> tooFewPrices = Arrays.asList(new MarketPrice(), new MarketPrice()); // nur 2 Einträge
        marketData.setData(tooFewPrices);
        service.cachedMarketData = marketData;
        invokeCalculateOptimalProductionWindow(service);
        assertNull(service.getOptimalProductionWindow());
    }
    
    // Hilfsmethode zum Aufrufen der privaten calculateOptimalProductionWindow-Methode
    private void invokeCalculateOptimalProductionWindow(AwattarService service) {
        try {
            var method = AwattarService.class.getDeclaredMethod("calculateOptimalProductionWindow");
            method.setAccessible(true);
            method.invoke(service);
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }
    
    // Hilfsmethode zum Erstellen von Testpreisen
    private List<MarketPrice> createTestPrices() {
        List<MarketPrice> prices = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            MarketPrice price = mock(MarketPrice.class);
            when(price.getMarketprice()).thenReturn((double) (100 + i * 10));
            when(price.getStart_timestamp()).thenReturn((long) i);
            when(price.getEnd_timestamp()).thenReturn((long) (i + 1));
            when(price.getPriceInEurPerKwh()).thenReturn((100.0 + i * 10) / 1000);
            prices.add(price);
        }
        return prices;
    }
}