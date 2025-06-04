package com.die_macher.pick_and_place.dobot.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit-Tests für die DobotProtocol-Klasse. Diese Tests überprüfen die Funktionalität der
 * Protokollkonstanten und -methoden.
 */
public class DobotProtocolTest {

  @Test
  @DisplayName("Sollte die Prüfsumme korrekt berechnen")
  public void testCalculateChecksum() {
    // Arrange
    byte[] data =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x03, // Befehl
          0x01, // Steuerung
          0x10,
          0x20,
          0x30 // Payload
        };

    // Act
    byte checksum = DobotProtocol.calculateChecksum(data);

    // Assert
    // Erwartete Prüfsumme: ~(0x03 + 0x01 + 0x10 + 0x20 + 0x30) + 1 = ~0x64 + 1 = 0x9C
    assertEquals((byte) 0x9C, checksum, "Die berechnete Prüfsumme sollte korrekt sein");
  }

  @Test
  @DisplayName("Sollte die Prüfsumme einer Nachricht validieren")
  public void testValidateChecksum() {
    // Arrange
    byte[] validResponse =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x03, // Befehl
          0x01, // Steuerung
          0x10,
          0x20,
          0x30, // Payload
          (byte) 0x9C // Prüfsumme
        };

    byte[] invalidResponse =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x03, // Befehl
          0x01, // Steuerung
          0x10,
          0x20,
          0x30, // Payload
          (byte) 0x00 // Falsche Prüfsumme
        };

    // Act & Assert
    assertTrue(
        DobotProtocol.validateChecksum(validResponse), "Gültige Prüfsumme sollte validiert werden");
    assertFalse(
        DobotProtocol.validateChecksum(invalidResponse),
        "Ungültige Prüfsumme sollte nicht validiert werden");
  }

  @Test
  @DisplayName("Sollte das Nachrichtenformat validieren")
  public void testValidateResponseFormat() {
    // Arrange
    DobotProtocol.Commands expectedCommand = DobotProtocol.Commands.GET_DEVICE_SN;

    byte[] validResponse =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x00, // Befehl (GET_DEVICE_SN)
          0x00, // Steuerung
          0x10,
          0x20,
          0x30, // Payload
          (byte) 0xA0 // Prüfsumme (berechnet für dieses Beispiel)
        };

    byte[] invalidHeader =
        new byte[] {
          (byte) 0xBB,
          (byte) 0xBB, // Falscher Header
          0x05, // Länge
          0x00, // Befehl
          0x00, // Steuerung
          0x10,
          0x20,
          0x30, // Payload
          (byte) 0xA0 // Prüfsumme
        };

    byte[] invalidCommand =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x01, // Falscher Befehl
          0x00, // Steuerung
          0x10,
          0x20,
          0x30, // Payload
          (byte) 0xA0 // Prüfsumme
        };

    byte[] tooShort =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x00 // Befehl (unvollständig)
        };

    // Act & Assert
    assertTrue(
        DobotProtocol.validateResponseFormat(validResponse, expectedCommand),
        "Gültiges Nachrichtenformat sollte validiert werden");
    assertFalse(
        DobotProtocol.validateResponseFormat(invalidHeader, expectedCommand),
        "Nachricht mit ungültigem Header sollte nicht validiert werden");
    assertFalse(
        DobotProtocol.validateResponseFormat(invalidCommand, expectedCommand),
        "Nachricht mit falschem Befehl sollte nicht validiert werden");
    assertFalse(
        DobotProtocol.validateResponseFormat(tooShort, expectedCommand),
        "Zu kurze Nachricht sollte nicht validiert werden");
    assertFalse(
        DobotProtocol.validateResponseFormat(null, expectedCommand),
        "Null-Nachricht sollte nicht validiert werden");
  }

  @Test
  @DisplayName("Sollte die Payload aus einer Antwort extrahieren")
  public void testExtractResponsePayload() {
    // Arrange
    byte[] response =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge (Payload-Größe + 2)
          0x00, // Befehl
          0x00, // Steuerung
          0x10,
          0x20,
          0x30, // Payload (3 Bytes)
          (byte) 0xA0 // Prüfsumme
        };

    byte[] expectedPayload = new byte[] {0x10, 0x20, 0x30};

    // Act
    byte[] extractedPayload = DobotProtocol.extractResponsePayload(response);

    // Assert
    assertNotNull(extractedPayload, "Extrahierte Payload sollte nicht null sein");
    assertArrayEquals(expectedPayload, extractedPayload, "Extrahierte Payload sollte korrekt sein");
  }

  @Test
  @DisplayName("Sollte null zurückgeben, wenn die Antwort ungültig ist")
  public void testExtractResponsePayloadWithInvalidResponse() {
    // Arrange
    byte[] tooShort =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x05, // Länge
          0x00 // Befehl (unvollständig)
        };

    byte[] invalidLength =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x01, // Ungültige Länge (zu klein)
          0x00, // Befehl
          0x00, // Steuerung
          0x10,
          0x20,
          0x30, // Payload
          (byte) 0xA0 // Prüfsumme
        };

    // Act & Assert
    assertNull(DobotProtocol.extractResponsePayload(null), "Null-Antwort sollte null zurückgeben");
    assertNull(
        DobotProtocol.extractResponsePayload(tooShort), "Zu kurze Antwort sollte null zurückgeben");
    assertNull(
        DobotProtocol.extractResponsePayload(invalidLength),
        "Antwort mit ungültiger Länge sollte null zurückgeben");
  }

  @Test
  @DisplayName("Sollte leere Payload korrekt extrahieren")
  public void testExtractEmptyPayload() {
    // Arrange
    byte[] response =
        new byte[] {
          (byte) 0xAA,
          (byte) 0xAA, // Header
          0x02, // Länge (keine Payload, nur Befehl + Steuerung)
          0x00, // Befehl
          0x00, // Steuerung
          (byte) 0xFE // Prüfsumme
        };

    // Act
    byte[] extractedPayload = DobotProtocol.extractResponsePayload(response);

    // Assert
    assertNotNull(extractedPayload, "Extrahierte Payload sollte nicht null sein");
    assertEquals(0, extractedPayload.length, "Extrahierte Payload sollte leer sein");
  }

  @ParameterizedTest
  @MethodSource("provideControlByteTestCases")
  @DisplayName("Sollte Steuerungsbytes korrekt erstellen")
  public void testCreateControlByte(boolean isWrite, boolean isQueued, byte expected) {
    // Act
    byte controlByte = DobotProtocol.ControlBits.createControlByte(isWrite, isQueued);

    // Assert
    assertEquals(
        expected,
        controlByte,
        String.format(
            "Steuerungsbyte für (isWrite=%s, isQueued=%s) sollte 0x%02X sein",
            isWrite, isQueued, expected));
  }

  private static Stream<Arguments> provideControlByteTestCases() {
    return Stream.of(
        Arguments.of(false, false, (byte) 0x00), // Lesen, nicht in Warteschlange
        Arguments.of(true, false, (byte) 0x01), // Schreiben, nicht in Warteschlange
        Arguments.of(false, true, (byte) 0x02), // Lesen, in Warteschlange
        Arguments.of(true, true, (byte) 0x03) // Schreiben, in Warteschlange
        );
  }
}
