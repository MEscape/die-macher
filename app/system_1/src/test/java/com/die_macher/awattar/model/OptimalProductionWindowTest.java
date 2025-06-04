package com.die_macher.awattar.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("OptimalProductionWindow Tests")
class OptimalProductionWindowTest {

  @Test
  @DisplayName("Konstruktor sollte Objekt mit Standardwerten korrekt initialisieren")
  void testConstructorWithDefaultValues() {
    // Vorbereitung
    long startTimestamp = System.currentTimeMillis();
    long endTimestamp = startTimestamp + 3600000; // +1 Stunde
    List<MarketData.MarketPrice> prices = new ArrayList<>();
    double totalCost = 42.5;

    // Ausführung
    OptimalProductionWindow window =
        new OptimalProductionWindow(startTimestamp, endTimestamp, prices, totalCost);

    // Überprüfung
    assertEquals(startTimestamp, window.getStartTimestamp());
    assertEquals(endTimestamp, window.getEndTimestamp());
    assertSame(prices, window.getPrices());
    assertEquals(totalCost, window.getTotalCost());
    assertEquals(0.0, window.getPriceInEurPerKwh()); // Standardwert
  }

  @Test
  @DisplayName("Konstruktor sollte Objekt mit spezifischem Preis pro kWh korrekt initialisieren")
  void testConstructorWithSpecificPricePerKwh() {
    // Vorbereitung
    long startTimestamp = System.currentTimeMillis();
    long endTimestamp = startTimestamp + 3600000; // +1 Stunde
    List<MarketData.MarketPrice> prices = new ArrayList<>();
    double totalCost = 42.5;
    double priceInEurPerKwh = 0.25;

    // Ausführung
    OptimalProductionWindow window =
        new OptimalProductionWindow(
            startTimestamp, endTimestamp, prices, totalCost, priceInEurPerKwh);

    // Überprüfung
    assertEquals(startTimestamp, window.getStartTimestamp());
    assertEquals(endTimestamp, window.getEndTimestamp());
    assertSame(prices, window.getPrices());
    assertEquals(totalCost, window.getTotalCost());
    assertEquals(priceInEurPerKwh, window.getPriceInEurPerKwh());
  }

  @Test
  @DisplayName("getStartTimeFormatted sollte korrekt formatierte Startzeit zurückgeben")
  void testGetStartTimeFormatted() {
    // Vorbereitung
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
    long timestamp = now.toInstant().toEpochMilli();
    String expected = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

    OptimalProductionWindow window =
        new OptimalProductionWindow(timestamp, timestamp + 3600000, new ArrayList<>(), 0.0);

    // Ausführung
    String result = window.getStartTimeFormatted();

    // Überprüfung
    assertEquals(expected, result);
  }

  @Test
  @DisplayName("getEndTimeFormatted sollte korrekt formatierte Endzeit zurückgeben")
  void testGetEndTimeFormatted() {
    // Vorbereitung
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
    long timestamp = now.toInstant().toEpochMilli();
    String expected = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

    OptimalProductionWindow window =
        new OptimalProductionWindow(timestamp - 3600000, timestamp, new ArrayList<>(), 0.0);

    // Ausführung
    String result = window.getEndTimeFormatted();

    // Überprüfung
    assertEquals(expected, result);
  }

  @ParameterizedTest
  @ValueSource(longs = {0, -1, -1000})
  @DisplayName("formatTimestamp sollte 'Invalid date' für ungültige Timestamps zurückgeben")
  void testFormatTimestampWithInvalidValues(long invalidTimestamp) {
    // Vorbereitung
    OptimalProductionWindow window =
        new OptimalProductionWindow(
            invalidTimestamp, System.currentTimeMillis(), new ArrayList<>(), 0.0);

    // Ausführung
    String result = window.getStartTimeFormatted();

    // Überprüfung
    assertEquals("Invalid date", result);
  }
}
