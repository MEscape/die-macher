package com.die_macher.pick_and_place.dobot.config;

import com.fazecast.jSerialComm.SerialPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class DobotSerialConnectorTest {

    @Mock
    private SerialPort serialPort;

    private DobotSerialConnector connector;
    private ByteArrayOutputStream outputStream;
    private ByteArrayInputStream inputStream;

    @BeforeEach
    void setUp() {
        connector = new DobotSerialConnector();
        outputStream = new ByteArrayOutputStream();
        inputStream = new ByteArrayInputStream(new byte[0]);
    }

    @Test
    @DisplayName("Should connect successfully to serial port")
    void testConnectSuccess() {
        // Arrange
        String portName = "COM3";
        int timeout = 1000;

        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            when(serialPort.getInputStream()).thenReturn(inputStream);
            when(serialPort.getOutputStream()).thenReturn(outputStream);

            // Act
            boolean result = connector.connect(portName, timeout);

            // Assert
            assertTrue(result, "Should return true for successful connection");
            verify(serialPort).setBaudRate(115200);
            verify(serialPort).setNumDataBits(8);
            verify(serialPort).setNumStopBits(1);
            verify(serialPort).setParity(SerialPort.NO_PARITY);
            verify(serialPort).setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeout, timeout);
            verify(serialPort).openPort();
            verify(serialPort).getInputStream();
            verify(serialPort).getOutputStream();
        }
    }

    @Test
    @DisplayName("Should fail to connect when port cannot be opened")
    void testConnectFailure() {
        // Arrange
        String portName = "COM3";
        int timeout = 1000;

        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(false);

            boolean result = connector.connect(portName, timeout);

            // Assert
            assertFalse(result, "Should return false when port cannot be opened");
            verify(serialPort, never()).getInputStream();
            verify(serialPort, never()).getOutputStream();
        }
    }

    @Test
    @DisplayName("Should disconnect successfully")
    void testDisconnect() {
        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            when(serialPort.isOpen()).thenReturn(true);
            when(serialPort.getInputStream()).thenReturn(inputStream);
            when(serialPort.getOutputStream()).thenReturn(outputStream);

            connector.connect("COM3", 1000);

            when(serialPort.closePort()).thenReturn(true);

            // Act
            connector.disconnect();

            // Assert
            verify(serialPort).closePort();
        }
    }

    @Test
    @DisplayName("Should check if connected correctly")
    void testIsConnected() {
        // Arrange - initially not connected
        assertFalse(connector.isConnected(), "Should initially return false for isConnected");

        // Connect
        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            AtomicBoolean open = new AtomicBoolean(true);
            when(serialPort.isOpen()).thenAnswer(invocation -> open.get());
            when(serialPort.getInputStream()).thenReturn(inputStream);
            when(serialPort.getOutputStream()).thenReturn(outputStream);
            connector.connect("COM3", 1000);

            // Assert - now connected
            assertTrue(connector.isConnected(), "Should return true after successful connection");

            // Disconnect
            when(serialPort.closePort()).thenAnswer(invocation -> {
                open.set(false);
                return true;
            });
            connector.disconnect();

            // Assert - now disconnected
            assertFalse(connector.isConnected(), "Should return false after disconnection");
        }
    }

    @Test
    @DisplayName("Should send data successfully")
    void testSendDataSuccess() {
        // Arrange - setup a connected state with a mock output stream
        ByteArrayOutputStream mockOutputStream = spy(new ByteArrayOutputStream());
        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            when(serialPort.isOpen()).thenReturn(true);
            when(serialPort.getInputStream()).thenReturn(inputStream);
            when(serialPort.getOutputStream()).thenReturn(mockOutputStream);
            connector.connect("COM3", 1000);

            // Act
            byte[] data = {0x01, 0x02, 0x03};
            boolean result = connector.sendData(data);

            // Assert
            assertTrue(result, "Should return true for successful data sending");
            assertEquals(3, mockOutputStream.size(), "Should write all bytes to the output stream");
        }
    }

    @Test
    @DisplayName("Should handle IOException when sending data")
    void testSendDataWithIOException() throws IOException {
        // Arrange - setup a connected state with a mock output stream that throws IOException
        OutputStream mockOutputStream = mock(OutputStream.class);
        doThrow(new IOException("Test exception")).when(mockOutputStream).write(any(byte[].class));

        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            when(serialPort.isOpen()).thenReturn(true);
            when(serialPort.getInputStream()).thenReturn(inputStream);
            when(serialPort.getOutputStream()).thenReturn(mockOutputStream);
            connector.connect("COM3", 1000);

            // Act
            byte[] data = {0x01, 0x02, 0x03};
            boolean result = connector.sendData(data);

            // Assert
            assertFalse(result, "Should return false when IOException occurs");
            verify(mockOutputStream).write(data);
        }
    }

    @Test
    @DisplayName("Should fail to send data when not connected")
    void testSendDataWhenNotConnected() {
        // Act
        byte[] data = {0x01, 0x02, 0x03};
        boolean result = connector.sendData(data);

        // Assert
        assertFalse(result, "Should return false when not connected");
    }

    @Test
    @DisplayName("Should read response successfully")
    void testReadResponseSuccess() {
        // Arrange - setup a connected state with a mock input stream containing response data
        byte[] responseData = {(byte) 0xAA, (byte) 0xBB, 0x03, 0x01, 0x00, 0x01, (byte) ((byte) 0xAA + (byte) 0xBB + 0x03 + 0x01 + 0x01)};
        ByteArrayInputStream mockInputStream = new ByteArrayInputStream(responseData);

        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            when(serialPort.isOpen()).thenReturn(true);
            when(serialPort.getInputStream()).thenReturn(mockInputStream);
            when(serialPort.getOutputStream()).thenReturn(outputStream);
            connector.connect("COM3", 1000);

            // Act
            byte[] result = connector.readData(1000);

            // Assert
            assertNotNull(result, "Should return non-null response");
            assertEquals(responseData.length, result.length, "Response length should match expected");
            assertArrayEquals(responseData, result, "Response data should match expected");
        }
    }

    @Test
    @DisplayName("Should return null when reading response fails")
    void testReadResponseFailure() throws IOException {
        // Arrange - setup a connected state with a mock input stream that throws IOException
        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.available()).thenReturn(1);
        when(mockInputStream.read(any(byte[].class))).thenThrow(new IOException("Test exception"));

        try (MockedStatic<SerialPort> serialPortMockedStatic = mockStatic(SerialPort.class)) {
            serialPortMockedStatic.when(() -> SerialPort.getCommPort(any())).thenReturn(serialPort);
            serialPortMockedStatic.when(SerialPort::getCommPorts).thenReturn(new SerialPort[]{serialPort});

            when(serialPort.openPort()).thenReturn(true);
            when(serialPort.isOpen()).thenReturn(true);
            when(serialPort.getInputStream()).thenReturn(mockInputStream);
            when(serialPort.getOutputStream()).thenReturn(outputStream);
            connector.connect("COM3", 1000);

            byte[] data = connector.readData(1000);

            assertNull(data, "Should return null when IOException occurs");
        }
    }
}