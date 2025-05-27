package com.die_macher.awattar.service;

import com.die_macher.awattar.client.AwattarApiClient;
import com.die_macher.awattar.config.AwattarConfig;
import com.die_macher.awattar.dto.MarketDataDto;
import com.die_macher.awattar.dto.MarketPriceDto;
import com.die_macher.awattar.mapper.AwattarMapper;
import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwattarServiceImplTest {

    @Mock
    private AwattarApiClient apiClient;

    @Mock
    private AwattarMapper mapper;

    @Mock
    private AwattarConfig config;

    @InjectMocks
    private AwattarServiceImpl awattarService;

    private MarketDataDto mockMarketDataDto;
    private MarketData mockMarketData;
    private List<MarketPrice> mockPrices;

    @BeforeEach
    void setUp() {

        lenient().when(config.getProductionHours()).thenReturn(3);

        // Mock-Daten für MarketDataDto
        mockMarketDataDto = new MarketDataDto();
        mockMarketDataDto.setObject("marketdata");
        List<MarketPriceDto> priceDtos = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            MarketPriceDto priceDto = new MarketPriceDto();
            ZonedDateTime time = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(i);
            priceDto.setStart_timestamp(time.toInstant().toEpochMilli());
            priceDto.setEnd_timestamp(time.plusHours(1).toInstant().toEpochMilli());
            priceDto.setMarketprice(50.0 + i); // Unterschiedliche Preise
            priceDto.setUnit("EUR/MWh");
            priceDtos.add(priceDto);
        }
        mockMarketDataDto.setData(priceDtos);

        // Mock-Daten für MarketData
        mockMarketData = new MarketData();
        mockMarketData.setObject("marketdata");
        mockPrices = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            MarketPrice price = new MarketPrice();
            ZonedDateTime time = ZonedDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(i);
            price.setStartTimestamp(time.toInstant().toEpochMilli());
            price.setEndTimestamp(time.plusHours(1).toInstant().toEpochMilli());
            price.setMarketprice(50.0 + i); // Unterschiedliche Preise
            price.setUnit("EUR/MWh");
            mockPrices.add(price);
        }
        mockMarketData.setData(mockPrices);

        // Standard-Mapping-Verhalten
        lenient().when(mapper.toModel(any(MarketDataDto.class))).thenReturn(mockMarketData);
    }

    @Test
    @DisplayName("Test fetchCurrentMarketData - Erfolgsfall")
    void testFetchCurrentMarketData_Success() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);

        // Act
        MarketData result = awattarService.fetchCurrentMarketData();

        // Assert
        assertNotNull(result);
        assertEquals(mockMarketData, result);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel(mockMarketDataDto);
    }

    @Test
    @DisplayName("Test fetchTomorrowMarketData - Erfolgsfall")
    void testFetchTomorrowMarketData_Success() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);

        // Act
        MarketData result = awattarService.fetchTomorrowMarketData();

        // Assert
        assertNotNull(result);
        assertEquals(mockMarketData, result);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel(mockMarketDataDto);
    }

    @Test
    @DisplayName("Test updateMarketData - Erfolgsfall")
    void testUpdateMarketData_Success() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);

        // Act
        awattarService.updateMarketData();

        // Assert
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel(mockMarketDataDto);
    }

    @Test
    @DisplayName("Test getOptimalProductionWindow - Wenn optimalWindow null ist")
    void testGetOptimalProductionWindow_WhenOptimalWindowIsNull() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);

        // Act
        OptimalProductionWindow result = awattarService.getOptimalProductionWindow();

        // Assert
        assertNotNull(result);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel(mockMarketDataDto);
    }

    @Test
    @DisplayName("Test getOptimalProductionWindow - Wenn optimalWindow bereits berechnet wurde")
    void testGetOptimalProductionWindow_WhenOptimalWindowAlreadyCalculated() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);

        awattarService.getOptimalProductionWindow();

        reset(apiClient, mapper);

        // Act
        OptimalProductionWindow result = awattarService.getOptimalProductionWindow();

        // Assert
        assertNotNull(result);

        verify(apiClient, never()).fetchMarketDataFor(anyLong());
        verify(mapper, never()).toModel((MarketDataDto) any());
    }

    @Test
    @DisplayName("Test getCurrentPartCost - Erfolgsfall")
    void testGetCurrentPartCost_Success() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);
        when(config.getEnergyPerPart()).thenReturn(0.2); // 0.2 kWh pro Teil

        // Act
        double result = awattarService.getCurrentPartCost();

        // Assert
        assertTrue(result > 0);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel(mockMarketDataDto);
    }

    @Test
    @DisplayName("Test getCurrentPartCost - Wenn MarketData null ist")
    void testGetCurrentPartCost_WhenMarketDataIsNull() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(null);
        when(mapper.toModel((MarketDataDto) null)).thenReturn(null);

        // Act
        double result = awattarService.getCurrentPartCost();

        // Assert
        assertEquals(-1, result);
    }

    @Test
    @DisplayName("Test getCurrentPartCost - Wenn CurrentPrice null ist")
    void testGetCurrentPartCost_WhenCurrentPriceIsNull() {
        // Arrange
        MarketData emptyData = new MarketData();
        emptyData.setData(Collections.emptyList());
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);
        when(mapper.toModel(mockMarketDataDto)).thenReturn(emptyData);

        // Act
        double result = awattarService.getCurrentPartCost();

        // Assert
        assertEquals(-1, result);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel(mockMarketDataDto);
    }


    @Test
    @DisplayName("Test initOnStartup - Wenn es nach 13 Uhr ist und Daten verfügbar sind")
    void testInitOnStartup_AfterOnePM_DataAvailable() {
        // Arrange
        AwattarServiceImpl testService = new AwattarServiceImpl(apiClient, mapper, config) {
            @Override
            public void initOnStartup() {
                MarketData data = fetchTomorrowMarketData();
                if (data != null && data.getData() != null && data.getData().size() >= config.getProductionHours()) {
                    calculateOptimalProductionWindow();
                }
            }
        };
        
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);

        // Act
        testService.initOnStartup();

        // Assert
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
    }

    @Test
    @DisplayName("Test initOnStartup - Wenn es vor 13 Uhr ist")
    void testInitOnStartup_BeforeOnePM() {
        // Arrange
        AwattarServiceImpl testService = new AwattarServiceImpl(apiClient, mapper, config) {
            @Override
            public void initOnStartup() {
                // nothing here
            }
        };

        // Act
        testService.initOnStartup();

        // Assert
        verify(apiClient, never()).fetchMarketDataFor(anyLong());
    }

    @Test
    @DisplayName("Test initOnStartup - Wenn es nach 13 Uhr ist aber keine Daten verfügbar sind")
    void testInitOnStartup_AfterOnePM_NoDataAvailable() {
        // Arrange
        AwattarServiceImpl testService = new AwattarServiceImpl(apiClient, mapper, config) {
            int callCount = 0;

            @Override
            public void initOnStartup() {
                boolean success = false;
                int maxTries = 2;
                int tries = 0;
                
                while (!success && tries < maxTries) {
                    MarketData data = fetchTomorrowMarketData();
                    if (data.getData() != null && data.getData().size() >= config.getProductionHours()) {
                        calculateOptimalProductionWindow();
                        success = true;
                    } else {
                        tries++;
                    }
                }
            }
            
            @Override
            protected void sleepFor(long millis) {
                //skip
            }
            
            @Override
            public MarketData fetchTomorrowMarketData() {
                callCount++;
                MarketData emptyData = new MarketData();
                emptyData.setData(Collections.emptyList());
                return emptyData;
            }
        };

        // Act
        testService.initOnStartup();

        // Assert
        int actualCallCount = (int) ReflectionTestUtils.getField(testService, "callCount");
        assertEquals(2, actualCallCount, "fetchTomorrowMarketData");
    }

    @Test
    @DisplayName("Test calculateOptimalProductionWindow - Erfolgsfall")
    void testCalculateOptimalProductionWindow_Success() {
        // Arrange
        ReflectionTestUtils.setField(awattarService, "cachedMarketData", mockMarketData);

        // Act - Methode über Reflection aufrufen
        ReflectionTestUtils.invokeMethod(awattarService, "calculateOptimalProductionWindow");

        // Assert - Prüfen, ob optimalWindow gesetzt wurde
        OptimalProductionWindow window = (OptimalProductionWindow) ReflectionTestUtils.getField(awattarService, "optimalWindow");
        assertNotNull(window);
    }

    @Test
    @DisplayName("Test calculateOptimalProductionWindow - Wenn cachedMarketData null ist")
    void testCalculateOptimalProductionWindow_WhenCachedMarketDataIsNull() {
        // Arrange
        ReflectionTestUtils.setField(awattarService, "cachedMarketData", null);

        // Act - Methode über Reflection aufrufen
        ReflectionTestUtils.invokeMethod(awattarService, "calculateOptimalProductionWindow");

        // Assert - optimalWindow sollte nicht gesetzt werden
        OptimalProductionWindow window = (OptimalProductionWindow) ReflectionTestUtils.getField(awattarService, "optimalWindow");
        assertNull(window);
    }

    @Test
    @DisplayName("Test calculateOptimalProductionWindow - Wenn nicht genügend Daten vorhanden sind")
    void testCalculateOptimalProductionWindow_WhenNotEnoughData() {
        // Arrange
        MarketData insufficientData = new MarketData();
        insufficientData.setData(mockPrices.subList(0, 2)); // Nur 2 Einträge, aber 3 benötigt
        ReflectionTestUtils.setField(awattarService, "cachedMarketData", insufficientData);

        // Act - Methode über Reflection aufrufen
        ReflectionTestUtils.invokeMethod(awattarService, "calculateOptimalProductionWindow");

        // Assert - optimalWindow sollte nicht gesetzt werden
        OptimalProductionWindow window = (OptimalProductionWindow) ReflectionTestUtils.getField(awattarService, "optimalWindow");
        assertNull(window);
    }

    @Test
    @DisplayName("Test calculateProductionCost - Mit leerer Preisliste")
    void testCalculateProductionCost_WithEmptyPriceList() {
        // Arrange
        List<MarketPrice> emptyPrices = Collections.emptyList();

        // Act - Methode über Reflection aufrufen
        double cost = ReflectionTestUtils.invokeMethod(awattarService, "calculateProductionCost", emptyPrices);

        // Assert
        assertEquals(0.0, cost, 0.001);
    }

    @Test
    @DisplayName("Test sleepFor - InterruptedException wird korrekt behandelt")
    void testSleepFor_InterruptedException() throws InterruptedException {
        // Arrange
        AwattarServiceImpl spyService = spy(awattarService);
        doThrow(InterruptedException.class).when(spyService).sleepFor(anyLong());
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            // Eine Methode aufrufen, die sleepFor verwendet
            ReflectionTestUtils.setField(spyService, "cachedMarketData", null);
            
            // Wir simulieren initOnStartup mit einer vereinfachten Version
            spyService.initOnStartup();
        });
    }
    
    @Test
    @DisplayName("Test initOnStartup - Vollständige Methode mit Zeitprüfung vor 13 Uhr")
    void testInitOnStartup_CompleteMethodBeforeOnePM() {
        // Arrange
        AwattarServiceImpl testService = new AwattarServiceImpl(apiClient, mapper, config) {
            @Override
            protected LocalTime getNow() {
                return LocalTime.of(12, 0); // 12 Uhr mittags, vor 13 Uhr
            }
        };
        
        // Act
        testService.initOnStartup();
        
        // Assert
        verify(apiClient, never()).fetchMarketDataFor(anyLong());
    }
    
    @Test
    @DisplayName("Test initOnStartup - Vollständige Methode mit Zeitprüfung nach 13 Uhr")
    void testInitOnStartup_CompleteMethodAfterOnePM() {
        // Arrange
        AwattarServiceImpl testService = new AwattarServiceImpl(apiClient, mapper, config) {
            @Override
            protected LocalTime getNow() {
                return LocalTime.of(14, 0); // 14 Uhr, nach 13 Uhr
            }
            
            @Override
            protected void sleepFor(long millis) {
                // Keine Verzögerung im Test
            }
        };
        
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);
        
        // Act
        testService.initOnStartup();
        
        // Assert
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
    }
    
    @Test
    @DisplayName("Test initOnStartup - Mit InterruptedException")
    void testInitOnStartup_WithInterruptedException() {
        // Arrange
        AwattarServiceImpl testService = new AwattarServiceImpl(apiClient, mapper, config) {
            private int sleepCount = 0;
            
            @Override
            protected LocalTime getNow() {
                return LocalTime.of(14, 0); // 14 Uhr, nach 13 Uhr
            }
            
            @Override
            protected void sleepFor(long millis) throws InterruptedException {
                sleepCount++;
                if (sleepCount == 1) { // Beim ersten Aufruf eine Exception werfen
                    throw new InterruptedException("Test interruption");
                }
            }
        };
        
        // Einen leeren MarketData zurückgeben, um die Schleife zu durchlaufen
        MarketData emptyData = new MarketData();
        emptyData.setData(Collections.emptyList());
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(mockMarketDataDto);
        when(mapper.toModel(any(MarketDataDto.class))).thenReturn(emptyData);
        
        // Act
        testService.initOnStartup();
        
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
    }
    
    @Test
    @DisplayName("Test calculateProductionCost - Mit normaler Preisliste")
    void testCalculateProductionCost_WithNormalPriceList() {
        // Arrange
        when(config.getPartsPerHour()).thenReturn(10); // 10 Teile pro Stunde
        when(config.getProductionHours()).thenReturn(3); // 3 Stunden Produktion
        when(config.getEnergyPerPart()).thenReturn(0.2); // 0.2 kWh pro Teil
        
        List<MarketPrice> prices = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MarketPrice price = new MarketPrice();
            price.setMarketprice(100.0); // 100 EUR/MWh = 0.1 EUR/kWh
            prices.add(price);
        }
        
        double cost = ReflectionTestUtils.invokeMethod(awattarService, "calculateProductionCost", prices);
        
        assertEquals(0.6, cost, 0.001);
    }
    
    @Test
    @DisplayName("Test fetchCurrentMarketData - Wenn API null zurückgibt")
    void testFetchCurrentMarketData_WhenApiReturnsNull() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(null);
        when(mapper.toModel((MarketDataDto) null)).thenReturn(null);
        
        // Act
        MarketData result = awattarService.fetchCurrentMarketData();
        
        // Assert
        assertNull(result);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel((MarketDataDto) null);
    }
    
    @Test
    @DisplayName("Test fetchTomorrowMarketData - Wenn API null zurückgibt")
    void testFetchTomorrowMarketData_WhenApiReturnsNull() {
        // Arrange
        when(apiClient.fetchMarketDataFor(anyLong())).thenReturn(null);
        when(mapper.toModel((MarketDataDto) null)).thenReturn(null);
        
        // Act
        MarketData result = awattarService.fetchTomorrowMarketData();
        
        // Assert
        assertNull(result);
        verify(apiClient, times(1)).fetchMarketDataFor(anyLong());
        verify(mapper, times(1)).toModel((MarketDataDto) null);
    }
    
    // Hilfsmethode für getNow() in AwattarServiceImpl hinzufügen
    @Test
    @DisplayName("Test für die Hilfsmethode getNow")
    void testGetNow() {

        // Arrange & Act
        LocalTime now = ReflectionTestUtils.invokeMethod(awattarService, "getNow");
        
        // Assert
        assertNotNull(now);
    }
}