package com.die_macher.awattar.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MarketData Tests")
class MarketDataTest {

  @Test
  @DisplayName("getCurrentPrice sollte null zurückgeben, wenn data null ist")
  void testGetCurrentPriceWithNullData() {
    // Vorbereitung
    MarketData marketData = new MarketData();
    marketData.setData(null);

    // Ausführung
    MarketData.MarketPrice result = marketData.getCurrentPrice();

    // Überprüfung
    assertNull(result);
  }

  @Test
  @DisplayName("getCurrentPrice sollte null zurückgeben, wenn data leer ist")
  void testGetCurrentPriceWithEmptyData() {
    // Vorbereitung
    MarketData marketData = new MarketData();
    marketData.setData(new ArrayList<>());

    // Ausführung
    MarketData.MarketPrice result = marketData.getCurrentPrice();

    // Überprüfung
    assertNull(result);
  }

  @Test
  @DisplayName("getCurrentPrice sollte das erste Element zurückgeben, wenn data nicht leer ist")
  void testGetCurrentPriceWithNonEmptyData() {
    // Vorbereitung
    MarketData marketData = new MarketData();
    List<MarketData.MarketPrice> prices = new ArrayList<>();

    MarketData.MarketPrice price1 = new MarketData.MarketPrice();
    price1.setMarketprice(42.0);
    prices.add(price1);

    MarketData.MarketPrice price2 = new MarketData.MarketPrice();
    price2.setMarketprice(43.0);
    prices.add(price2);

    marketData.setData(prices);

    // Ausführung
    MarketData.MarketPrice result = marketData.getCurrentPrice();

    // Überprüfung
    assertNotNull(result);
    assertSame(price1, result);
    assertEquals(42.0, result.getMarketprice());
  }

  @Test
  @DisplayName("Setter und Getter für object sollten korrekt funktionieren")
  void testObjectSetterAndGetter() {
    // Vorbereitung
    MarketData marketData = new MarketData();
    String expectedObject = "testObject";

    // Ausführung
    marketData.setObject(expectedObject);
    String result = marketData.getObject();

    // Überprüfung
    assertEquals(expectedObject, result);
  }

  @Test
  @DisplayName("Setter und Getter für data sollten korrekt funktionieren")
  void testDataSetterAndGetter() {
    // Vorbereitung
    MarketData marketData = new MarketData();
    List<MarketData.MarketPrice> expectedData = new ArrayList<>();
    expectedData.add(new MarketData.MarketPrice());

    // Ausführung
    marketData.setData(expectedData);
    List<MarketData.MarketPrice> result = marketData.getData();

    // Überprüfung
    assertSame(expectedData, result);
  }

  @Nested
  @DisplayName("MarketPrice Tests")
  class MarketPriceTest {

    @Test
    @DisplayName("Setter und Getter für startTimestamp sollten korrekt funktionieren")
    void testStartTimestampSetterAndGetter() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      long expectedTimestamp = System.currentTimeMillis();

      // Ausführung
      price.setStartTimestamp(expectedTimestamp);
      long result = price.getStartTimestamp();

      // Überprüfung
      assertEquals(expectedTimestamp, result);
    }

    @Test
    @DisplayName("Setter und Getter für endTimestamp sollten korrekt funktionieren")
    void testEndTimestampSetterAndGetter() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      long expectedTimestamp = System.currentTimeMillis();

      // Ausführung
      price.setEndTimestamp(expectedTimestamp);
      long result = price.getEndTimestamp();

      // Überprüfung
      assertEquals(expectedTimestamp, result);
    }

    @Test
    @DisplayName("Setter und Getter für marketprice sollten korrekt funktionieren")
    void testMarketpriceSetterAndGetter() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      double expectedPrice = 42.5;

      // Ausführung
      price.setMarketprice(expectedPrice);
      double result = price.getMarketprice();

      // Überprüfung
      assertEquals(expectedPrice, result);
    }

    @Test
    @DisplayName("Setter und Getter für unit sollten korrekt funktionieren")
    void testUnitSetterAndGetter() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      String expectedUnit = "EUR/MWh";

      // Ausführung
      price.setUnit(expectedUnit);
      String result = price.getUnit();

      // Überprüfung
      assertEquals(expectedUnit, result);
    }

    @Test
    @DisplayName("getStartTimeFormatted sollte korrekt formatierte Startzeit zurückgeben")
    void testGetStartTimeFormatted() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Vienna"));
      long timestamp = now.atZone(ZoneId.of("Europe/Vienna")).toInstant().toEpochMilli();
      String expected = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

      price.setStartTimestamp(timestamp);

      // Ausführung
      String result = price.getStartTimeFormatted();

      // Überprüfung
      assertEquals(expected, result);
    }

    @Test
    @DisplayName("getEndTimeFormatted sollte korrekt formatierte Endzeit zurückgeben")
    void testGetEndTimeFormatted() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Vienna"));
      long timestamp = now.atZone(ZoneId.of("Europe/Vienna")).toInstant().toEpochMilli();
      String expected = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

      price.setEndTimestamp(timestamp);

      // Ausführung
      String result = price.getEndTimeFormatted();

      // Überprüfung
      assertEquals(expected, result);
    }

    @Test
    @DisplayName(
        "getPriceInEurPerKwh sollte korrekte Umrechnung von EUR/MWh zu EUR/kWh durchführen")
    void testGetPriceInEurPerKwh() {
      // Vorbereitung
      MarketData.MarketPrice price = new MarketData.MarketPrice();
      double marketPrice = 42.0; // EUR/MWh
      double expected = 0.042; // EUR/kWh (42 / 1000)

      price.setMarketprice(marketPrice);

      // Ausführung
      double result = price.getPriceInEurPerKwh();

      // Überprüfung
      assertEquals(expected, result, 0.0001);
    }
  }
}
