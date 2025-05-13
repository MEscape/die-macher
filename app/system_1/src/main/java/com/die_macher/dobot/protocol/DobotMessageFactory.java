package com.die_macher.dobot.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Factory class for creating Dobot protocol messages.
 * Handles the creation of properly formatted message packets for different commands.
 */
public class DobotMessageFactory {
    /**
     * Creates a GetDeviceSN command message according to protocol specifications.
     *
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createGetDeviceSNMessage() {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + [Payload] + Checksum(1)
        byte[] message = new byte[5]; // No payload for this command

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (2 bytes for command ID + control byte, little endian)
        message[2] = 0x02;

        // Command ID (Get Device SN = 0, little endian)
        message[3] = 0x00;

        // Control byte (Read operation, not queued)
        message[4] = DobotProtocol.ControlBits.createControlByte(false, false);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a GetDeviceName command message.
     *
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createGetDeviceNameMessage() {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + [Payload] + Checksum(1)
        byte[] message = new byte[5]; // No payload for this command

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (2 bytes for command ID + control byte, little endian)
        message[2] = 0x02;

        // Command ID (Get Device Name = 1, little endian)
        message[3] = 0x01;

        // Control byte (Read operation, not queued)
        message[4] = DobotProtocol.ControlBits.createControlByte(false, false);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Utility method to parse device SN from response.
     *
     * @param response the raw response from the Dobot
     * @return the device serial number as a string, or null if parsing failed
     */
    public static String parseDeviceSNFromResponse(byte[] response) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_DEVICE_SN)) {
            return null;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        if (payload == null || payload.length == 0) {
            return null;
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    /**
     * Utility method to parse device name from response.
     *
     * @param response the raw response from the Dobot
     * @return the device name as a string, or null if parsing failed
     */
    public static String parseDeviceNameFromResponse(byte[] response) {
        System.out.println(Arrays.toString(response));
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_DEVICE_NAME)) {
            return null;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        if (payload == null || payload.length == 0) {
            return null;
        }

        return new String(payload, StandardCharsets.UTF_8);
    }
}