package com.die_macher.dobot.protocol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit-Tests für die DobotMessageFactory-Klasse.
 * Diese Tests überprüfen die korrekte Erstellung verschiedener Dobot-Protokollnachrichten.
 */
public class DobotMessageFactoryTest {

    @Test
    @DisplayName("Sollte eine GET_DEVICE_SN-Nachricht korrekt erstellen")
    public void testCreateGetDeviceSNMessage() {
        // Act
        byte[] message = DobotMessageFactory.createGetDeviceSNMessage();
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.GET_DEVICE_SN.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte GET_DEVICE_SN sein");
        assertEquals(0x00, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x00 sein (Lesen, nicht in Warteschlange)");
        assertEquals(6, message.length, "Die Gesamtlänge sollte 6 sein (keine Payload)");
    }

    @Test
    @DisplayName("Sollte eine GET_DEVICE_NAME-Nachricht korrekt erstellen")
    public void testCreateGetDeviceNameMessage() {
        // Act
        byte[] message = DobotMessageFactory.createGetDeviceNameMessage();
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.GET_DEVICE_NAME.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte GET_DEVICE_NAME sein");
        assertEquals(0x00, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x00 sein (Lesen, nicht in Warteschlange)");
        assertEquals(6, message.length, "Die Gesamtlänge sollte 6 sein (keine Payload)");
    }

    @Test
    @DisplayName("Sollte eine SET_PTP_CMD-Nachricht korrekt erstellen")
    public void testCreateSetPTPCmdMessage() {
        // Arrange
        float x = 123.45f;
        float y = 67.89f;
        float z = 10.11f;
        float r = 45.0f;
        boolean isQueued = true;
        
        // Act
        byte[] message = DobotMessageFactory.createSetPTPCmdMessage(x, y, z, r, isQueued);
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_PTP_CMD.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_PTP_CMD sein");
        assertEquals(0x03, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x03 sein (Schreiben, in Warteschlange)");
        
        // Überprüfe die Payload-Länge (4 floats * 4 bytes)
        assertEquals(2 + 16, message[DobotProtocol.Indices.LENGTH_INDEX], 
                "Die Länge sollte 2 + 16 sein (Befehl + Steuerung + 4 floats)");
        
        // Extrahiere und überprüfe die Float-Werte aus der Payload
        ByteBuffer buffer = ByteBuffer.wrap(message, DobotProtocol.Indices.PAYLOAD_INDEX, 16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(x, buffer.getFloat(), 0.001f, "X-Koordinate sollte korrekt sein");
        assertEquals(y, buffer.getFloat(), 0.001f, "Y-Koordinate sollte korrekt sein");
        assertEquals(z, buffer.getFloat(), 0.001f, "Z-Koordinate sollte korrekt sein");
        assertEquals(r, buffer.getFloat(), 0.001f, "R-Koordinate sollte korrekt sein");
    }

    @Test
    @DisplayName("Sollte eine SET_HOME_CMD-Nachricht korrekt erstellen")
    public void testCreateSetHomeCmdMessage() {
        // Arrange
        boolean isQueued = true;
        
        // Act
        byte[] message = DobotMessageFactory.createSetHomeCmdMessage(isQueued);
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_HOME_CMD.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_HOME_CMD sein");
        assertEquals(0x03, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x03 sein (Schreiben, in Warteschlange)");
        
        // Überprüfe die Payload-Länge (4 bytes für reservierten Int)
        assertEquals(2 + 4, message[DobotProtocol.Indices.LENGTH_INDEX], 
                "Die Länge sollte 2 + 4 sein (Befehl + Steuerung + reservierter Int)");
        
        // Extrahiere und überprüfe den Int-Wert aus der Payload (sollte 0 sein)
        ByteBuffer buffer = ByteBuffer.wrap(message, DobotProtocol.Indices.PAYLOAD_INDEX, 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0, buffer.getInt(), "Der reservierte Int-Wert sollte 0 sein");
    }

    @Test
    @DisplayName("Sollte eine SET_END_EFFECTOR_SUCTION_CUP-Nachricht korrekt erstellen")
    public void testCreateSetEndEffectorSuctionCupMessage() {
        // Arrange
        boolean isSucked = true;
        boolean isQueued = true;
        
        // Act
        byte[] message = DobotMessageFactory.createSetEndEffectorSuctionCupMessage(isSucked, isQueued);
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_END_EFFECTOR_SUCTION_CUP.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_END_EFFECTOR_SUCTION_CUP sein");
        assertEquals(0x03, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x03 sein (Schreiben, in Warteschlange)");
        
        // Überprüfe die Payload-Länge (2 bytes für die beiden Booleans)
        assertEquals(2 + 2, message[DobotProtocol.Indices.LENGTH_INDEX], 
                "Die Länge sollte 2 + 2 sein (Befehl + Steuerung + 2 Booleans)");
        
        // Überprüfe die Boolean-Werte in der Payload
        assertEquals(0x01, message[DobotProtocol.Indices.PAYLOAD_INDEX], 
                "Der erste Boolean-Wert sollte 1 sein (true)");
        assertEquals(0x01, message[DobotProtocol.Indices.PAYLOAD_INDEX + 1], 
                "Der zweite Boolean-Wert sollte 1 sein (true)");
        
        // Test mit isSucked = false
        message = DobotMessageFactory.createSetEndEffectorSuctionCupMessage(false, isQueued);
        assertEquals(0x00, message[DobotProtocol.Indices.PAYLOAD_INDEX], 
                "Der erste Boolean-Wert sollte 0 sein (false)");
        assertEquals(0x00, message[DobotProtocol.Indices.PAYLOAD_INDEX + 1], 
                "Der zweite Boolean-Wert sollte 0 sein (false)");
    }

    @Test
    @DisplayName("Sollte Warteschlangen-Steuerungsnachrichten korrekt erstellen")
    public void testCreateQueueControlMessages() {
        // Act & Assert für Start-Nachricht
        byte[] startMessage = DobotMessageFactory.createSetQueuedCmdStartExecMessage();
        assertNotNull(startMessage, "Die Start-Nachricht sollte nicht null sein");
        assertEquals((byte) DobotProtocol.Commands.SET_QUEUED_CMD_START_EXEC.getValue(), startMessage[DobotProtocol.Indices.COMMAND_INDEX],
                "Der Befehlsindex sollte SET_QUEUED_CMD_START_EXEC sein");
        assertEquals(0x01, startMessage[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x01 sein (Schreiben, nicht in Warteschlange)");
        
        // Act & Assert für Stop-Nachricht
        byte[] stopMessage = DobotMessageFactory.createSetQueuedCmdStopMessage();
        assertNotNull(stopMessage, "Die Stop-Nachricht sollte nicht null sein");
        assertEquals((byte) DobotProtocol.Commands.SET_QUEUED_CMD_STOP.getValue(), stopMessage[DobotProtocol.Indices.COMMAND_INDEX],
                "Der Befehlsindex sollte SET_QUEUED_CMD_STOP sein");
        assertEquals(0x01, stopMessage[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x01 sein (Schreiben, nicht in Warteschlange)");
        
        // Act & Assert für Clear-Nachricht
        byte[] clearMessage = DobotMessageFactory.createSetQueuedCmdClearMessage();
        assertNotNull(clearMessage, "Die Clear-Nachricht sollte nicht null sein");
        assertEquals((byte) DobotProtocol.Commands.SET_QUEUED_CMD_CLEAR.getValue(), clearMessage[DobotProtocol.Indices.COMMAND_INDEX],
                "Der Befehlsindex sollte SET_QUEUED_CMD_CLEAR sein");
        assertEquals(0x01, clearMessage[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x01 sein (Schreiben, nicht in Warteschlange)");
    }

    @Test
    @DisplayName("Sollte eine SET_HOME_PARAMS-Nachricht korrekt erstellen")
    public void testCreateSetHomeParamsMessage() {
        // Act
        byte[] message = DobotMessageFactory.createSetHomeParamsMessage(true);
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_HOME_PARAMS.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_HOME_PARAMS sein");
        assertEquals(0x01, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x01 sein (Schreiben, nicht in Warteschlange)");
        
        // Überprüfe die Payload-Länge (4 floats * 4 bytes)
        assertEquals(2 + 16, message[DobotProtocol.Indices.LENGTH_INDEX], 
                "Die Länge sollte 2 + 16 sein (Befehl + Steuerung + 4 floats)");
        
        // Extrahiere und überprüfe die Float-Werte aus der Payload
        ByteBuffer buffer = ByteBuffer.wrap(message, DobotProtocol.Indices.PAYLOAD_INDEX, 16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(0.0f, buffer.getFloat(), 0.001f, "X-Koordinate sollte 0.0 sein");
        assertEquals(45.0f, buffer.getFloat(), 0.001f, "Y-Koordinate sollte 45.0 sein");
        assertEquals(45.0f, buffer.getFloat(), 0.001f, "Z-Koordinate sollte 45.0 sein");
        assertEquals(0.0f, buffer.getFloat(), 0.001f, "R-Koordinate sollte 0.0 sein");
    }

    @Test
    @DisplayName("Sollte PTP-Konfigurationsnachrichten korrekt erstellen")
    public void testCreatePTPConfigMessages() {
        // Arrange
        float velocity = 100.0f;
        float acceleration = 80.0f;
        float velocityRatio = 50.0f;
        float accelerationRatio = 50.0f;
        
        // Act & Assert für Koordinaten-Parameter
        byte[] coordMessage = DobotMessageFactory.createSetPTPCoordinateParamsMessage(
                velocity, velocity, acceleration, acceleration);
        assertNotNull(coordMessage, "Die Koordinaten-Parameter-Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_PTP_COORDINATE_PARAMS.getValue(), 
                coordMessage[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_PTP_COORDINATE_PARAMS sein");
        
        // Überprüfe die Float-Werte in der Payload
        ByteBuffer coordBuffer = ByteBuffer.wrap(coordMessage, DobotProtocol.Indices.PAYLOAD_INDEX, 16);
        coordBuffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(velocity, coordBuffer.getFloat(), 0.001f, "XYZ-Geschwindigkeit sollte korrekt sein");
        assertEquals(velocity, coordBuffer.getFloat(), 0.001f, "R-Geschwindigkeit sollte korrekt sein");
        assertEquals(acceleration, coordBuffer.getFloat(), 0.001f, "XYZ-Beschleunigung sollte korrekt sein");
        assertEquals(acceleration, coordBuffer.getFloat(), 0.001f, "R-Beschleunigung sollte korrekt sein");
        
        // Act & Assert für Joint-Parameter
        byte[] jointMessage = DobotMessageFactory.createSetPTPJointParamsMessage(velocity, acceleration);
        assertNotNull(jointMessage, "Die Joint-Parameter-Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_PTP_JOINT_PARAMS.getValue(), 
                jointMessage[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_PTP_JOINT_PARAMS sein");
        
        // Überprüfe die Float-Werte in der Payload (8 floats: 4 für Geschwindigkeit, 4 für Beschleunigung)
        ByteBuffer jointBuffer = ByteBuffer.wrap(jointMessage, DobotProtocol.Indices.PAYLOAD_INDEX, 32);
        jointBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 4; i++) {
            assertEquals(velocity, jointBuffer.getFloat(), 0.001f, 
                    "Geschwindigkeit für Joint " + i + " sollte korrekt sein");
        }
        for (int i = 0; i < 4; i++) {
            assertEquals(acceleration, jointBuffer.getFloat(), 0.001f, 
                    "Beschleunigung für Joint " + i + " sollte korrekt sein");
        }
        
        // Act & Assert für Common-Parameter
        byte[] commonMessage = DobotMessageFactory.createSetPTPCommonParamsMessage(
                velocityRatio, accelerationRatio);
        assertNotNull(commonMessage, "Die Common-Parameter-Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_PTP_COMMON_PARAMS.getValue(), 
                commonMessage[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_PTP_COMMON_PARAMS sein");
        
        // Überprüfe die Float-Werte in der Payload
        ByteBuffer commonBuffer = ByteBuffer.wrap(commonMessage, DobotProtocol.Indices.PAYLOAD_INDEX, 8);
        commonBuffer.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(velocityRatio, commonBuffer.getFloat(), 0.001f, "Geschwindigkeitsverhältnis sollte korrekt sein");
        assertEquals(accelerationRatio, commonBuffer.getFloat(), 0.001f, "Beschleunigungsverhältnis sollte korrekt sein");
    }

    @Test
    @DisplayName("Sollte eine SET_DEVICE_NAME-Nachricht korrekt erstellen")
    public void testCreateSetDeviceNameMessage() {
        // Arrange
        String deviceName = "TestDobot123";
        byte[] expectedPayload = deviceName.getBytes(StandardCharsets.UTF_8);
        
        // Act
        byte[] message = DobotMessageFactory.createSetDeviceNameMessage(deviceName);
        
        // Assert
        assertNotNull(message, "Die erstellte Nachricht sollte nicht null sein");
        assertEquals(DobotProtocol.Commands.SET_DEVICE_NAME.getValue(), message[DobotProtocol.Indices.COMMAND_INDEX], 
                "Der Befehlsindex sollte SET_DEVICE_NAME sein");
        assertEquals(0x01, message[DobotProtocol.Indices.CONTROL_INDEX], 
                "Der Steuerungsbyte sollte 0x01 sein (Schreiben, nicht in Warteschlange)");
        
        // Überprüfe die Payload-Länge
        assertEquals(2 + expectedPayload.length, message[DobotProtocol.Indices.LENGTH_INDEX], 
                "Die Länge sollte 2 + Payload-Länge sein");
        
        // Überprüfe die Payload (Gerätename als Bytes)
        byte[] actualPayload = new byte[expectedPayload.length];
        System.arraycopy(message, DobotProtocol.Indices.PAYLOAD_INDEX, actualPayload, 0, expectedPayload.length);
        assertArrayEquals(expectedPayload, actualPayload, "Die Payload sollte den Gerätenamen als Bytes enthalten");
    }
}
