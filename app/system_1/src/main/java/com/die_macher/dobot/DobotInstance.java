package com.die_macher.dobot;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class DobotInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(DobotInstance.class);

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final DobotProperties dobotProperties;

    @Autowired
    public DobotInstance(final DobotProperties dobotPropertiesLocal) {
        this.dobotProperties = dobotPropertiesLocal;
    }

    @PostConstruct
    public void initialize() {
        try {
            connectToDobot();
            if (pingDobot()) {
                LOGGER.info("Dobot successfully initialized and responding on port: {}", dobotProperties.getPortName());
            } else {
                LOGGER.warn("Dobot initialized but not responding to ping on port: {}", dobotProperties.getPortName());
            }
        } catch (Exception e) {
            LOGGER.info("Fehler bei der Initialisierung des Dobot: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        disconnectFromDobot();
    }

    private void connectToDobot() {
        LOGGER.info("Verbinde mit Dobot auf Port: {}", dobotProperties.getPortName());

        // Verfügbare Ports auflisten
        SerialPort[] ports = SerialPort.getCommPorts();
        LOGGER.info("Verfügbare serielle Ports:");
        for (SerialPort port : ports) {
            LOGGER.info("  - {} ({})", port.getSystemPortName(), port.getDescriptivePortName());
        }

        // Port öffnen
        serialPort = SerialPort.getCommPort(dobotProperties.getPortName());
        serialPort.setBaudRate(dobotProperties.getBaudRate());
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);

        if (!serialPort.openPort()) {
            throw new RuntimeException("Konnte den seriellen Port nicht öffnen: " + dobotProperties.getPortName());
        }

        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
    }

    private void disconnectFromDobot() {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                LOGGER.error("Fehler beim Schließen der Streams: {}", e.getMessage(), e);
            }

            serialPort.closePort();
            LOGGER.info("Verbindung zum Dobot getrennt");
        }
    }

    public boolean pingDobot() {
        LOGGER.info("Sending GetDeviceSN command to Dobot");

        try {
            // GetDeviceSN command as shown in Table 5
            byte[] message = {
                    (byte)0xAA, (byte)0xAA,     // Header
                    (byte)0x02, (byte)0x00,     // Length (2+0 in little endian)
                    (byte)0x00, (byte)0x00,     // Command ID (0)
                    (byte)0x00                  // Control byte (rw=0, isQueued=0)
            };

            // Calculate checksum (sum of all bytes except header, mod 256)
            int checksum = 0;
            for (int i = 2; i < message.length; i++) {
                checksum += (message[i] & 0xFF);
            }
            checksum = checksum % 256;

            // Send the command with checksum
            outputStream.write(message);
            outputStream.write((byte)checksum);
            outputStream.flush();

            LOGGER.debug("Sent command: Header=0xAA 0xAA, Len=0x02 0x00, ID=0x00 0x00, Ctrl=0x00, Checksum=0x{}",
                    String.format("%02X", checksum));

            // Wait for response
            Thread.sleep(200);

            // Process response
            if (inputStream.available() > 0) {
                byte[] buffer = new byte[256];
                int bytesRead = inputStream.read(buffer);

                // Print response as hex
                StringBuilder hexResponse = new StringBuilder();
                for (int i = 0; i < bytesRead; i++) {
                    hexResponse.append(String.format("%02X ", buffer[i] & 0xFF));
                }
                LOGGER.info("Received response: {}", hexResponse.toString());

                // Validate response format (should match Table 6)
                if (bytesRead >= 4 &&
                        buffer[0] == (byte)0xAA && buffer[1] == (byte)0xAA && // Header
                        buffer[4] == (byte)0x00 && buffer[5] == (byte)0x00) { // Command ID

                    // Extract device SN from response
                    if (bytesRead > 7) {  // Header(2) + Len(2) + ID(2) + Ctrl(1) + at least 1 char
                        int snLength = ((buffer[2] & 0xFF) | ((buffer[3] & 0xFF) << 8)) - 2;
                        if (snLength > 0 && snLength <= bytesRead - 7) {
                            StringBuilder sn = new StringBuilder();
                            for (int i = 0; i < snLength; i++) {
                                sn.append((char)buffer[7 + i]);
                            }
                            LOGGER.info("Device SN: {}", sn.toString());
                        }
                    }

                    return true;
                } else {
                    LOGGER.warn("Invalid response format from Dobot");
                    return false;
                }
            } else {
                LOGGER.warn("No response received from Dobot");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Error while pinging Dobot: {}", e.getMessage(), e);
            return false;
        }
    }
}
