package com.die_macher.awattar.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.die_macher.awattar.dto.MarketDataDto;
import com.die_macher.awattar.dto.MarketPriceDto;
import com.die_macher.awattar.dto.OptimalProductionWindowDto;
import com.die_macher.awattar.model.MarketData;
import com.die_macher.awattar.model.MarketData.MarketPrice;
import com.die_macher.awattar.model.OptimalProductionWindow;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AwattarMapperTest {

  private AwattarMapper mapper;

  // Testdaten
  private static final long START_TIMESTAMP = 1609459200000L; // 2021-01-01 00:00:00
  private static final long END_TIMESTAMP = 1609462800000L; // 2021-01-01 01:00:00
  private static final double MARKET_PRICE = 42.42;
  private static final String UNIT = "EUR/MWh";
  private static final double TOTAL_COST = 126.26;
  private static final double PRICE_IN_EUR_PER_KWH = 0.04242;
  private static final String OBJECT_TYPE = "list";

  @BeforeEach
  void setUp() {
    mapper = new AwattarMapper();
  }

  @Test
  @DisplayName("toDto(MarketData) sollte null zurückgeben, wenn MarketData null ist")
  void toDto_WhenMarketDataIsNull_ShouldReturnNull() {
    // Act
    MarketDataDto result = mapper.toDto((MarketData) null);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn MarketData null ist");
  }

  @Test
  @DisplayName("toDto(MarketData) sollte korrekt konvertieren, wenn MarketData gültig ist")
  void toDto_WhenMarketDataIsValid_ShouldConvertCorrectly() {
    // Arrange
    MarketData marketData = createMarketData();

    // Act
    MarketDataDto result = mapper.toDto(marketData);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        OBJECT_TYPE, result.getObject(), "Das Objekt-Feld sollte korrekt übertragen werden");
    assertNotNull(result.getData(), "Die Daten-Liste sollte nicht null sein");
    assertEquals(2, result.getData().size(), "Die Anzahl der Preisdaten sollte korrekt sein");

    MarketPriceDto firstPrice = result.getData().getFirst();
    assertEquals(
        START_TIMESTAMP,
        firstPrice.getStart_timestamp(),
        "Der Start-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        END_TIMESTAMP,
        firstPrice.getEnd_timestamp(),
        "Der End-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        MARKET_PRICE,
        firstPrice.getMarketprice(),
        "Der Marktpreis sollte korrekt übertragen werden");
    assertEquals(UNIT, firstPrice.getUnit(), "Die Einheit sollte korrekt übertragen werden");
  }

  @Test
  @DisplayName("toDto(MarketData) sollte korrekt konvertieren, wenn MarketData.data null ist")
  void toDto_WhenMarketDataHasNullData_ShouldConvertCorrectly() {
    // Arrange
    MarketData marketData = new MarketData();
    marketData.setObject(OBJECT_TYPE);
    marketData.setData(null);

    // Act
    MarketDataDto result = mapper.toDto(marketData);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        OBJECT_TYPE, result.getObject(), "Das Objekt-Feld sollte korrekt übertragen werden");
    assertNull(result.getData(), "Die Daten-Liste sollte null sein");
  }

  @Test
  @DisplayName("toDto(MarketPrice) sollte null zurückgeben, wenn MarketPrice null ist")
  void toDto_WhenMarketPriceIsNull_ShouldReturnNull() {
    // Act
    MarketPriceDto result = mapper.toDto((MarketPrice) null);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn MarketPrice null ist");
  }

  @Test
  @DisplayName("toDto(MarketPrice) sollte korrekt konvertieren, wenn MarketPrice gültig ist")
  void toDto_WhenMarketPriceIsValid_ShouldConvertCorrectly() {
    // Arrange
    MarketPrice marketPrice = createMarketPrice();

    // Act
    MarketPriceDto result = mapper.toDto(marketPrice);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        START_TIMESTAMP,
        result.getStart_timestamp(),
        "Der Start-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        END_TIMESTAMP,
        result.getEnd_timestamp(),
        "Der End-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        MARKET_PRICE, result.getMarketprice(), "Der Marktpreis sollte korrekt übertragen werden");
    assertEquals(UNIT, result.getUnit(), "Die Einheit sollte korrekt übertragen werden");
  }

  @Test
  @DisplayName("toModel(MarketDataDto) sollte null zurückgeben, wenn MarketDataDto null ist")
  void toModel_WhenMarketDataDtoIsNull_ShouldReturnNull() {
    // Act
    MarketData result = mapper.toModel((MarketDataDto) null);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn MarketDataDto null ist");
  }

  @Test
  @DisplayName("toModel(MarketDataDto) sollte korrekt konvertieren, wenn MarketDataDto gültig ist")
  void toModel_WhenMarketDataDtoIsValid_ShouldConvertCorrectly() {
    // Arrange
    MarketDataDto dto = createMarketDataDto();

    // Act
    MarketData result = mapper.toModel(dto);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        OBJECT_TYPE, result.getObject(), "Das Objekt-Feld sollte korrekt übertragen werden");
    assertNotNull(result.getData(), "Die Daten-Liste sollte nicht null sein");
    assertEquals(2, result.getData().size(), "Die Anzahl der Preisdaten sollte korrekt sein");

    MarketPrice firstPrice = result.getData().getFirst();
    assertEquals(
        START_TIMESTAMP,
        firstPrice.getStartTimestamp(),
        "Der Start-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        END_TIMESTAMP,
        firstPrice.getEndTimestamp(),
        "Der End-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        MARKET_PRICE,
        firstPrice.getMarketprice(),
        "Der Marktpreis sollte korrekt übertragen werden");
    assertEquals(UNIT, firstPrice.getUnit(), "Die Einheit sollte korrekt übertragen werden");
  }

  @Test
  @DisplayName(
      "toModel(MarketDataDto) sollte korrekt konvertieren, wenn MarketDataDto.data null ist")
  void toModel_WhenMarketDataDtoHasNullData_ShouldConvertCorrectly() {
    // Arrange
    MarketDataDto dto = new MarketDataDto();
    dto.setObject(OBJECT_TYPE);
    dto.setData(null);

    // Act
    MarketData result = mapper.toModel(dto);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        OBJECT_TYPE, result.getObject(), "Das Objekt-Feld sollte korrekt übertragen werden");
    assertNull(result.getData(), "Die Daten-Liste sollte null sein");
  }

  @Test
  @DisplayName("toModel(MarketPriceDto) sollte null zurückgeben, wenn MarketPriceDto null ist")
  void toModel_WhenMarketPriceDtoIsNull_ShouldReturnNull() {
    // Act
    MarketPrice result = mapper.toModel((MarketPriceDto) null);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn MarketPriceDto null ist");
  }

  @Test
  @DisplayName(
      "toModel(MarketPriceDto) sollte korrekt konvertieren, wenn MarketPriceDto gültig ist")
  void toModel_WhenMarketPriceDtoIsValid_ShouldConvertCorrectly() {
    // Arrange
    MarketPriceDto dto = createMarketPriceDto();

    // Act
    MarketPrice result = mapper.toModel(dto);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        START_TIMESTAMP,
        result.getStartTimestamp(),
        "Der Start-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        END_TIMESTAMP,
        result.getEndTimestamp(),
        "Der End-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        MARKET_PRICE, result.getMarketprice(), "Der Marktpreis sollte korrekt übertragen werden");
    assertEquals(UNIT, result.getUnit(), "Die Einheit sollte korrekt übertragen werden");
  }

  @Test
  @DisplayName(
      "toDto(OptimalProductionWindow) sollte null zurückgeben, wenn OptimalProductionWindow null ist")
  void toDto_WhenOptimalProductionWindowIsNull_ShouldReturnNull() {
    // Act
    OptimalProductionWindowDto result = mapper.toDto((OptimalProductionWindow) null);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn OptimalProductionWindow null ist");
  }

  @Test
  @DisplayName(
      "toDto(OptimalProductionWindow) sollte korrekt konvertieren, wenn OptimalProductionWindow gültig ist")
  void toDto_WhenOptimalProductionWindowIsValid_ShouldConvertCorrectly() {
    // Arrange
    OptimalProductionWindow window = createOptimalProductionWindow();

    // Act
    OptimalProductionWindowDto result = mapper.toDto(window);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        START_TIMESTAMP,
        result.getStartTimestamp(),
        "Der Start-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        END_TIMESTAMP,
        result.getEndTimestamp(),
        "Der End-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        TOTAL_COST, result.getTotalCost(), "Die Gesamtkosten sollten korrekt übertragen werden");
    assertEquals(
        PRICE_IN_EUR_PER_KWH,
        result.getPriceInEurPerKwh(),
        "Der Preis in EUR/kWh sollte korrekt übertragen werden");
    assertNotNull(
        result.getStartTimeFormatted(), "Die formatierte Startzeit sollte nicht null sein");
    assertNotNull(result.getEndTimeFormatted(), "Die formatierte Endzeit sollte nicht null sein");
    assertNotNull(result.getPrices(), "Die Preisliste sollte nicht null sein");
    assertEquals(2, result.getPrices().size(), "Die Anzahl der Preisdaten sollte korrekt sein");
  }

  @Test
  @DisplayName(
      "toDto(OptimalProductionWindow) sollte korrekt konvertieren, wenn OptimalProductionWindow.prices null ist")
  void toDto_WhenOptimalProductionWindowHasNullPrices_ShouldConvertCorrectly() {
    // Arrange
    OptimalProductionWindow window =
        new OptimalProductionWindow(
            START_TIMESTAMP, END_TIMESTAMP, null, TOTAL_COST, PRICE_IN_EUR_PER_KWH);

    // Act
    OptimalProductionWindowDto result = mapper.toDto(window);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(
        START_TIMESTAMP,
        result.getStartTimestamp(),
        "Der Start-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        END_TIMESTAMP,
        result.getEndTimestamp(),
        "Der End-Timestamp sollte korrekt übertragen werden");
    assertEquals(
        TOTAL_COST, result.getTotalCost(), "Die Gesamtkosten sollten korrekt übertragen werden");
    assertEquals(
        PRICE_IN_EUR_PER_KWH,
        result.getPriceInEurPerKwh(),
        "Der Preis in EUR/kWh sollte korrekt übertragen werden");
    assertNotNull(
        result.getStartTimeFormatted(), "Die formatierte Startzeit sollte nicht null sein");
    assertNotNull(result.getEndTimeFormatted(), "Die formatierte Endzeit sollte nicht null sein");
    assertNull(result.getPrices(), "Die Preisliste sollte null sein");
  }

  // Hilfsmethoden zum Erstellen von Testdaten

  private MarketData createMarketData() {
    MarketData marketData = new MarketData();
    marketData.setObject(OBJECT_TYPE);

    List<MarketPrice> prices = Arrays.asList(createMarketPrice(), createMarketPrice());
    marketData.setData(prices);

    return marketData;
  }

  private MarketPrice createMarketPrice() {
    MarketPrice price = new MarketPrice();
    price.setStartTimestamp(START_TIMESTAMP);
    price.setEndTimestamp(END_TIMESTAMP);
    price.setMarketprice(MARKET_PRICE);
    price.setUnit(UNIT);
    return price;
  }

  private MarketDataDto createMarketDataDto() {
    MarketDataDto dto = new MarketDataDto();
    dto.setObject(OBJECT_TYPE);

    List<MarketPriceDto> prices = Arrays.asList(createMarketPriceDto(), createMarketPriceDto());
    dto.setData(prices);

    return dto;
  }

  private MarketPriceDto createMarketPriceDto() {
    MarketPriceDto dto = new MarketPriceDto();
    dto.setStart_timestamp(START_TIMESTAMP);
    dto.setEnd_timestamp(END_TIMESTAMP);
    dto.setMarketprice(MARKET_PRICE);
    dto.setUnit(UNIT);
    return dto;
  }

  private OptimalProductionWindow createOptimalProductionWindow() {
    List<MarketPrice> prices = Arrays.asList(createMarketPrice(), createMarketPrice());

    return new OptimalProductionWindow(
        START_TIMESTAMP, END_TIMESTAMP, prices, TOTAL_COST, PRICE_IN_EUR_PER_KWH);
  }
}
