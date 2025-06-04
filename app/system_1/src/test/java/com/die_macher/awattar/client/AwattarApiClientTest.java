package com.die_macher.awattar.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.die_macher.awattar.config.AwattarConfig;
import com.die_macher.awattar.dto.MarketDataDto;
import com.die_macher.awattar.dto.MarketPriceDto;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AwattarApiClientTest {

  @Mock private RestTemplate restTemplate;

  @Mock private AwattarConfig config;

  private AwattarApiClient apiClient;

  private static final String API_BASE_URL = "https://api.awattar.at/v1/marketdata";
  private static final long START_MILLIS = 1609459200000L; // 2021-01-01 00:00:00
  private static final long END_MILLIS = START_MILLIS + 24 * 60 * 60 * 1000; // 24 hours later

  @BeforeEach
  void setUp() {
    when(config.getApiBaseUrl()).thenReturn(API_BASE_URL);
    apiClient = new AwattarApiClient(restTemplate, config);
  }

  @Test
  @DisplayName("Sollte Marktdaten erfolgreich abrufen, wenn API-Aufruf erfolgreich ist")
  void fetchMarketDataFor_WhenApiCallSucceeds_ShouldReturnMarketData() {
    // Arrange
    MarketDataDto expectedData = createSampleMarketData();
    ResponseEntity<MarketDataDto> responseEntity =
        new ResponseEntity<>(expectedData, HttpStatus.OK);

    String expectedUrl = API_BASE_URL + "?start=" + START_MILLIS + "&end=" + END_MILLIS;
    when(restTemplate.getForEntity(eq(expectedUrl), eq(MarketDataDto.class)))
        .thenReturn(responseEntity);

    // Act
    MarketDataDto result = apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert
    assertNotNull(result, "Das Ergebnis sollte nicht null sein");
    assertEquals(expectedData, result, "Das Ergebnis sollte den erwarteten Daten entsprechen");
    assertEquals(2, result.getData().size(), "Die Anzahl der Preisdaten sollte korrekt sein");

    // Verify
    verify(restTemplate).getForEntity(eq(expectedUrl), eq(MarketDataDto.class));
    verify(config).getApiBaseUrl();
  }

  @Test
  @DisplayName("Sollte URL mit korrekten Start- und Endparametern erstellen")
  void fetchMarketDataFor_ShouldCreateCorrectUrlWithStartAndEndParameters() {
    // Arrange
    when(restTemplate.getForEntity(anyString(), eq(MarketDataDto.class)))
        .thenReturn(new ResponseEntity<>(new MarketDataDto(), HttpStatus.OK));

    // Act
    apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert & Verify
    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(restTemplate).getForEntity(urlCaptor.capture(), eq(MarketDataDto.class));

    String capturedUrl = urlCaptor.getValue();
    assertTrue(
        capturedUrl.contains("start=" + START_MILLIS),
        "URL sollte den korrekten Start-Parameter enthalten");
    assertTrue(
        capturedUrl.contains("end=" + END_MILLIS),
        "URL sollte den korrekten End-Parameter enthalten");
  }

  @Test
  @DisplayName("Sollte null zurückgeben, wenn API-Aufruf eine HttpClientErrorException wirft")
  void fetchMarketDataFor_WhenHttpClientErrorExceptionOccurs_ShouldReturnNull() {
    // Arrange
    String expectedUrl = API_BASE_URL + "?start=" + START_MILLIS + "&end=" + END_MILLIS;
    when(restTemplate.getForEntity(eq(expectedUrl), eq(MarketDataDto.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    // Act
    MarketDataDto result = apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert
    assertNull(
        result, "Das Ergebnis sollte null sein, wenn eine HttpClientErrorException auftritt");

    // Verify
    verify(restTemplate).getForEntity(eq(expectedUrl), eq(MarketDataDto.class));
  }

  @Test
  @DisplayName("Sollte null zurückgeben, wenn API-Aufruf eine ResourceAccessException wirft")
  void fetchMarketDataFor_WhenResourceAccessExceptionOccurs_ShouldReturnNull() {
    // Arrange
    String expectedUrl = API_BASE_URL + "?start=" + START_MILLIS + "&end=" + END_MILLIS;
    when(restTemplate.getForEntity(eq(expectedUrl), eq(MarketDataDto.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    // Act
    MarketDataDto result = apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn eine ResourceAccessException auftritt");

    // Verify
    verify(restTemplate).getForEntity(eq(expectedUrl), eq(MarketDataDto.class));
  }

  @Test
  @DisplayName("Sollte null zurückgeben, wenn API-Antwort einen null-Body hat")
  void fetchMarketDataFor_WhenResponseBodyIsNull_ShouldReturnNull() {
    // Arrange
    ResponseEntity<MarketDataDto> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
    String expectedUrl = API_BASE_URL + "?start=" + START_MILLIS + "&end=" + END_MILLIS;
    when(restTemplate.getForEntity(eq(expectedUrl), eq(MarketDataDto.class)))
        .thenReturn(responseEntity);

    // Act
    MarketDataDto result = apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn der Response-Body null ist");

    // Verify
    verify(restTemplate).getForEntity(eq(expectedUrl), eq(MarketDataDto.class));
  }

  @Test
  @DisplayName("Sollte null zurückgeben, wenn eine allgemeine Exception auftritt")
  void fetchMarketDataFor_WhenGeneralExceptionOccurs_ShouldReturnNull() {
    // Arrange
    String expectedUrl = API_BASE_URL + "?start=" + START_MILLIS + "&end=" + END_MILLIS;
    when(restTemplate.getForEntity(eq(expectedUrl), eq(MarketDataDto.class)))
        .thenThrow(new RuntimeException("Unerwarteter Fehler"));

    // Act
    MarketDataDto result = apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert
    assertNull(result, "Das Ergebnis sollte null sein, wenn eine allgemeine Exception auftritt");

    // Verify
    verify(restTemplate).getForEntity(eq(expectedUrl), eq(MarketDataDto.class));
  }

  @Test
  @DisplayName("Sollte korrekte Endzeit berechnen (24 Stunden nach Startzeit)")
  void fetchMarketDataFor_ShouldCalculateCorrectEndTime() {
    // Arrange
    when(restTemplate.getForEntity(anyString(), eq(MarketDataDto.class)))
        .thenReturn(new ResponseEntity<>(new MarketDataDto(), HttpStatus.OK));

    // Act
    apiClient.fetchMarketDataFor(START_MILLIS);

    // Assert & Verify
    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(restTemplate).getForEntity(urlCaptor.capture(), eq(MarketDataDto.class));

    String capturedUrl = urlCaptor.getValue();
    assertTrue(
        capturedUrl.contains("end=" + END_MILLIS),
        "URL sollte den korrekten End-Parameter enthalten (24 Stunden nach Start)");
  }

  /** Hilfsmethode zum Erstellen von Beispiel-Marktdaten für Tests */
  private MarketDataDto createSampleMarketData() {
    MarketDataDto marketData = new MarketDataDto();
    marketData.setObject("list");

    MarketPriceDto price1 = new MarketPriceDto();
    price1.setStart_timestamp(START_MILLIS);
    price1.setEnd_timestamp(START_MILLIS + 3600000); // 1 hour later
    price1.setMarketprice(42.42);
    price1.setUnit("EUR/MWh");

    MarketPriceDto price2 = new MarketPriceDto();
    price2.setStart_timestamp(START_MILLIS + 3600000);
    price2.setEnd_timestamp(START_MILLIS + 7200000); // 2 hours later
    price2.setMarketprice(45.67);
    price2.setUnit("EUR/MWh");

    List<MarketPriceDto> priceList = Arrays.asList(price1, price2);
    marketData.setData(priceList);

    return marketData;
  }
}
