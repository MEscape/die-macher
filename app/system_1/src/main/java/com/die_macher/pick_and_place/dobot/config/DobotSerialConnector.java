package com.die_macher.pick_and_place.dobot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Handles low-level serial communication with the Dobot device.
 * This class is responsible for opening/closing connections and sending/receiving raw bytes.
 */
public class DobotSerialConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(DobotSerialConnector.class);

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Opens a connection to the specified serial port.
     *
     * @param portName the name of the serial port
     * @param timeout the connection timeout in milliseconds
     * @return true if connection was successful, false otherwise
     */
    public boolean connect(String portName, int timeout) {
        LOGGER.info("Connecting to Dobot on port: {}", portName);

        // List available ports
        logAvailablePorts();

        // Open port
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(115200);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(1);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, timeout, timeout);

        if (!serialPort.openPort()) {
            LOGGER.error("Failed to open serial port: {}", portName);
            return false;
        }

        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();

        LOGGER.info("Successfully connected to port: {}", portName);
        return true;
    }

    /**
     * Logs all available serial ports for debugging purposes.
     */
    private void logAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        LOGGER.info("Available serial ports:");
        for (SerialPort port : ports) {
            LOGGER.info("  - {} ({})", port.getSystemPortName(), port.getDescriptivePortName());
        }
    }

    /**
     * Disconnects from the serial port.
     */
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                LOGGER.error("Error closing streams: {}", e.getMessage(), e);
            }

            serialPort.closePort();
            LOGGER.info("Disconnected from Dobot");
        }
    }

    /**
     * Sends raw bytes to the device.
     *
     * @param data the bytes to send
     * @return true if sending was successful, false otherwise
     */
    public boolean sendData(byte[] data) {
        if (serialPort == null || !serialPort.isOpen()) {
            LOGGER.error("Cannot send data: Serial port not open");
            return false;
        }

        try {
            outputStream.write(data);
            outputStream.flush();

            // Log sent data for debugging
            logByteArray("Sent data", data);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error sending data: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Reads data from the device.
     *
     * @param timeout milliseconds to wait for data
     * @return byte array containing the read data, or null if no data available
     */
    public byte[] readData(int timeout) {
        if (serialPort == null || !serialPort.isOpen()) {
            LOGGER.error("Cannot read data: Serial port not open");
            return null;
        }

        try {
            // Wait for data to arrive
            Thread.sleep(timeout);

            if (inputStream.available() > 0) {
                byte[] buffer = new byte[256];
                int bytesRead = inputStream.read(buffer);

                byte[] result = Arrays.copyOf(buffer, bytesRead);
                logByteArray("Received data", result);
                return result;
            }

            return new byte[0]; // Empty array if no data available
        } catch (Exception e) {
            LOGGER.error("Error reading data: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Utility method for logging byte arrays in hex format.
     *
     * @param label descriptive label for the log
     * @param data byte array to log
     */
    private void logByteArray(String label, byte[] data) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            hexString.append(String.format("%02X ", b & 0xFF));
        }
        LOGGER.debug("{}: {}", label, hexString);
    }

    /**
     * Checks if the serial port is currently connected.
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        final boolean isConnected = serialPort != null && serialPort.isOpen();
        LOGGER.debug("Dobot connection status: {}", isConnected ? "Connected" : "Not Connected");

        return isConnected;
    }
}