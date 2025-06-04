package com.die_macher.awattar.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import com.die_macher.awattar.service.AwattarService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AwattarControllerTest {

  @Mock private AwattarService awattarService;

  @InjectMocks private AwattarController awattarController;

  private MarketData marketData;
  private MarketPrice marketPrice;
  private OptimalProductionWindow optimalWindow;

  @BeforeEach
  void setUp() {
    // Erstellen von Testdaten für MarketData
    marketData = new MarketData();
    marketData.setObject("list");

    marketPrice = new MarketPrice();
    marketPrice.setStartTimestamp(1609459200000L); // 2021-01-01 00:00:00
    marketPrice.setEndTimestamp(1609462800000L); // 2021-01-01 01:00:00
    marketPrice.setMarketprice(42.42);
    marketPrice.setUnit("EUR/MWh");

    List<MarketPrice> prices = new ArrayList<>();
    prices.add(marketPrice);
    marketData.setData(prices);

    // Erstellen von Testdaten für OptimalProductionWindow
    optimalWindow =
        new OptimalProductionWindow(
            1609459200000L, // 2021-01-01 00:00:00
            1609470000000L, // 2021-01-01 03:00:00
            prices,
            126.26, // Gesamtkosten
            0.04242 // Preis in EUR/kWh
            );
  }

  @Test
  @DisplayName("getMarketData sollte MarketData zurückgeben, wenn Daten verfügbar sind")
  void getMarketData_WhenDataAvailable_ShouldReturnOkWithData() {
    // Arrange
    when(awattarService.fetchTomorrowMarketData()).thenReturn(marketData);

    // Act
    ResponseEntity<MarketData> response = awattarController.getMarketData();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Der Status-Code sollte OK sein");
    assertNotNull(response.getBody(), "Der Response-Body sollte nicht null sein");
    assertEquals(
        marketData,
        response.getBody(),
        "Der Response-Body sollte die erwarteten Marktdaten enthalten");

    // Verify
    verify(awattarService).fetchTomorrowMarketData();
  }

  @Test
  @DisplayName("getMarketData sollte 404 zurückgeben, wenn keine Daten verfügbar sind")
  void getMarketData_WhenNoDataAvailable_ShouldReturnNotFound() {
    // Arrange
    when(awattarService.fetchTomorrowMarketData()).thenReturn(null);

    // Act
    ResponseEntity<MarketData> response = awattarController.getMarketData();

    // Assert
    assertEquals(
        HttpStatus.NOT_FOUND, response.getStatusCode(), "Der Status-Code sollte NOT_FOUND sein");
    assertNull(response.getBody(), "Der Response-Body sollte null sein");

    // Verify
    verify(awattarService).fetchTomorrowMarketData();
  }

  @Test
  @DisplayName(
      "getCurrentMarketData sollte aktuellen Marktpreis zurückgeben, wenn Daten verfügbar sind")
  void getCurrentMarketData_WhenDataAvailable_ShouldReturnOkWithCurrentPrice() {
    // Arrange
    when(awattarService.fetchCurrentMarketData()).thenReturn(marketData);

    // Act
    ResponseEntity<MarketPrice> response = awattarController.getCurrentMarketData();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Der Status-Code sollte OK sein");
    assertNotNull(response.getBody(), "Der Response-Body sollte nicht null sein");
    assertEquals(
        marketPrice,
        response.getBody(),
        "Der Response-Body sollte den aktuellen Marktpreis enthalten");

    // Verify
    verify(awattarService).fetchCurrentMarketData();
  }

  @Test
  @DisplayName("getCurrentMarketData sollte 404 zurückgeben, wenn keine Marktdaten verfügbar sind")
  void getCurrentMarketData_WhenNoMarketDataAvailable_ShouldReturnNotFound() {
    // Arrange
    when(awattarService.fetchCurrentMarketData()).thenReturn(null);

    // Act
    ResponseEntity<MarketPrice> response = awattarController.getCurrentMarketData();

    // Assert
    assertEquals(
        HttpStatus.NOT_FOUND, response.getStatusCode(), "Der Status-Code sollte NOT_FOUND sein");
    assertNull(response.getBody(), "Der Response-Body sollte null sein");

    // Verify
    verify(awattarService).fetchCurrentMarketData();
  }

  @Test
  @DisplayName(
      "getCurrentMarketData sollte 404 zurückgeben, wenn Marktdaten ohne Preise verfügbar sind")
  void getCurrentMarketData_WhenMarketDataHasNoCurrentPrice_ShouldReturnNotFound() {
    // Arrange
    MarketData emptyMarketData = new MarketData();
    emptyMarketData.setObject("list");
    emptyMarketData.setData(new ArrayList<>()); // Leere Preisliste

    when(awattarService.fetchCurrentMarketData()).thenReturn(emptyMarketData);

    // Act
    ResponseEntity<MarketPrice> response = awattarController.getCurrentMarketData();

    // Assert
    assertEquals(
        HttpStatus.NOT_FOUND, response.getStatusCode(), "Der Status-Code sollte NOT_FOUND sein");
    assertNull(response.getBody(), "Der Response-Body sollte null sein");

    // Verify
    verify(awattarService).fetchCurrentMarketData();
  }

  @Test
  @DisplayName(
      "getCurrentMarketData sollte 404 zurückgeben, wenn Marktdaten mit null-Preisliste verfügbar sind")
  void getCurrentMarketData_WhenMarketDataHasNullPriceList_ShouldReturnNotFound() {
    // Arrange
    MarketData nullListMarketData = new MarketData();
    nullListMarketData.setObject("list");
    nullListMarketData.setData(null); // Null-Preisliste

    when(awattarService.fetchCurrentMarketData()).thenReturn(nullListMarketData);

    // Act
    ResponseEntity<MarketPrice> response = awattarController.getCurrentMarketData();

    // Assert
    assertEquals(
        HttpStatus.NOT_FOUND, response.getStatusCode(), "Der Status-Code sollte NOT_FOUND sein");
    assertNull(response.getBody(), "Der Response-Body sollte null sein");

    // Verify
    verify(awattarService).fetchCurrentMarketData();
  }

  @Test
  @DisplayName(
      "getOptimalProductionWindow sollte optimales Produktionsfenster zurückgeben, wenn verfügbar")
  void getOptimalProductionWindow_WhenDataAvailable_ShouldReturnOkWithWindow() {
    // Arrange
    when(awattarService.getOptimalProductionWindow()).thenReturn(optimalWindow);

    // Act
    ResponseEntity<OptimalProductionWindow> response =
        awattarController.getOptimalProductionWindow();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Der Status-Code sollte OK sein");
    assertNotNull(response.getBody(), "Der Response-Body sollte nicht null sein");
    assertEquals(
        optimalWindow,
        response.getBody(),
        "Der Response-Body sollte das optimale Produktionsfenster enthalten");

    // Verify
    verify(awattarService).getOptimalProductionWindow();
  }

  @Test
  @DisplayName(
      "getOptimalProductionWindow sollte 404 zurückgeben, wenn kein optimales Fenster verfügbar ist")
  void getOptimalProductionWindow_WhenNoDataAvailable_ShouldReturnNotFound() {
    // Arrange
    when(awattarService.getOptimalProductionWindow()).thenReturn(null);

    // Act
    ResponseEntity<OptimalProductionWindow> response =
        awattarController.getOptimalProductionWindow();

    // Assert
    assertEquals(
        HttpStatus.NOT_FOUND, response.getStatusCode(), "Der Status-Code sollte NOT_FOUND sein");
    assertNull(response.getBody(), "Der Response-Body sollte null sein");

    // Verify
    verify(awattarService).getOptimalProductionWindow();
  }
}
