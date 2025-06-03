package com.die_macher.pick_and_place.dobot.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DobotMessageBuilderTest {

    @Test
    @DisplayName("Sollte eine Nachricht mit Befehl, aber ohne Payload erstellen")
    void testBuildMessageWithoutPayload() {
        // Arrange
        DobotProtocol.Commands command = DobotProtocol.Commands.GET_DEVICE_SN;
        boolean isWrite = false;
        boolean isQueued = false;

        // Act
        byte[] message = DobotMessageBuilder.command(command)
                .control(isWrite, isQueued)
                .build();

        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.HEADER[0], message[0], "Der erste Header-Byte sollte korrekt sein");
        assertEquals(DobotProtocol.HEADER[1], message[1], "Der zweite Header-Byte sollte korrekt sein");
        assertEquals(2, message[DobotProtocol.Indices.LENGTH_INDEX], "Die Länge sollte 2 sein (Befehl + Steuerung)");
        assertEquals(command.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], "Der Befehlsindex sollte korrekt sein");
        assertEquals(0x00, message[DobotProtocol.Indices.CONTROL_INDEX], "Der Steuerungsbyte sollte 0x00 sein");
        
        // Überprüfe die Prüfsumme
        byte expectedChecksum = DobotProtocol.calculateChecksum(new byte[] {
            message[0], message[1], message[2], message[3], message[4]
        });
        assertEquals(expectedChecksum, message[message.length - 1], "Die Prüfsumme sollte korrekt sein");
    }

    @Test
    @DisplayName("Sollte eine Nachricht mit Befehl, Steuerungsbits und Payload erstellen")
    void testBuildMessageWithPayload() {
        // Arrange
        DobotProtocol.Commands command = DobotProtocol.Commands.SET_PTP_CMD;
        boolean isWrite = true;
        boolean isQueued = true;
        byte[] payload = new byte[] {0x01, 0x02, 0x03, 0x04};

        // Act
        byte[] message = DobotMessageBuilder.command(command)
                .control(isWrite, isQueued)
                .payload(payload)
                .build();

        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.HEADER[0], message[0], "Der erste Header-Byte sollte korrekt sein");
        assertEquals(DobotProtocol.HEADER[1], message[1], "Der zweite Header-Byte sollte korrekt sein");
        assertEquals(2 + payload.length, message[DobotProtocol.Indices.LENGTH_INDEX], "Die Länge sollte 2 + Payload-Länge sein");
        assertEquals(command.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], "Der Befehlsindex sollte korrekt sein");
        assertEquals(0x03, message[DobotProtocol.Indices.CONTROL_INDEX], "Der Steuerungsbyte sollte 0x03 sein");
        
        // Überprüfe die Payload
        for (int i = 0; i < payload.length; i++) {
            assertEquals(payload[i], message[DobotProtocol.Indices.PAYLOAD_INDEX + i], 
                    "Payload-Byte an Position " + i + " sollte korrekt sein");
        }
        
        // Überprüfe die Prüfsumme
        byte[] messageWithoutChecksum = new byte[message.length - 1];
        System.arraycopy(message, 0, messageWithoutChecksum, 0, messageWithoutChecksum.length);
        byte expectedChecksum = DobotProtocol.calculateChecksum(messageWithoutChecksum);
        assertEquals(expectedChecksum, message[message.length - 1], "Die Prüfsumme sollte korrekt sein");
    }

    @Test
    @DisplayName("Sollte eine Nachricht mit null-Payload korrekt behandeln")
    void testBuildMessageWithNullPayload() {
        // Arrange
        DobotProtocol.Commands command = DobotProtocol.Commands.SET_HOME_CMD;
        boolean isWrite = true;
        boolean isQueued = false;

        // Act
        byte[] message = DobotMessageBuilder.command(command)
                .control(isWrite, isQueued)
                .payload(null)
                .build();

        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(2, message[DobotProtocol.Indices.LENGTH_INDEX], "Die Länge sollte 2 sein (Befehl + Steuerung)");
        assertEquals(command.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], "Der Befehlsindex sollte korrekt sein");
        assertEquals(0x01, message[DobotProtocol.Indices.CONTROL_INDEX], "Der Steuerungsbyte sollte 0x01 sein");
        assertEquals(6, message.length, "Die Gesamtlänge sollte 6 sein (Header + Länge + Befehl + Steuerung + Prüfsumme)");
    }

    @Test
    @DisplayName("Sollte verschiedene Steuerungsbyte-Kombinationen korrekt erstellen")
    void testControlByteVariations() {
        // Arrange & Act
        byte[] message1 = DobotMessageBuilder.command(DobotProtocol.Commands.GET_DEVICE_SN)
                .control(false, false)
                .build();
        
        byte[] message2 = DobotMessageBuilder.command(DobotProtocol.Commands.GET_DEVICE_SN)
                .control(true, false)
                .build();
        
        byte[] message3 = DobotMessageBuilder.command(DobotProtocol.Commands.GET_DEVICE_SN)
                .control(false, true)
                .build();
        
        byte[] message4 = DobotMessageBuilder.command(DobotProtocol.Commands.GET_DEVICE_SN)
                .control(true, true)
                .build();

        // Assert
        assertEquals(0x00, message1[DobotProtocol.Indices.CONTROL_INDEX], "Steuerungsbyte für (false, false) sollte 0x00 sein");
        assertEquals(0x01, message2[DobotProtocol.Indices.CONTROL_INDEX], "Steuerungsbyte für (true, false) sollte 0x01 sein");
        assertEquals(0x02, message3[DobotProtocol.Indices.CONTROL_INDEX], "Steuerungsbyte für (false, true) sollte 0x02 sein");
        assertEquals(0x03, message4[DobotProtocol.Indices.CONTROL_INDEX], "Steuerungsbyte für (true, true) sollte 0x03 sein");
    }

    @Test
    @DisplayName("Sollte die Fluent-API korrekt verketten")
    void testFluentApiChaining() {
        // Arrange
        DobotProtocol.Commands command = DobotProtocol.Commands.SET_DEVICE_NAME;
        boolean isWrite = true;
        boolean isQueued = false;
        byte[] payload = new byte[] {0x41, 0x42, 0x43}; // "ABC"

        // Act & Assert - Überprüfe, dass die Verkettung funktioniert
        assertDoesNotThrow(() -> {
            DobotMessageBuilder.command(command)
                    .control(isWrite, isQueued)
                    .payload(payload)
                    .build();
        }, "Die Fluent-API-Verkettung sollte keine Exception werfen");

        // Überprüfe, dass die Reihenfolge der Verkettung egal ist (außer command() muss zuerst sein)
        byte[] message1 = DobotMessageBuilder.command(command)
                .control(isWrite, isQueued)
                .payload(payload)
                .build();

        byte[] message2 = DobotMessageBuilder.command(command)
                .payload(payload)
                .control(isWrite, isQueued)
                .build();

        // Beide Nachrichten sollten identisch sein
        assertArrayEquals(message1, message2, "Die Reihenfolge der Methodenaufrufe sollte das Ergebnis nicht beeinflussen");
    }
}
