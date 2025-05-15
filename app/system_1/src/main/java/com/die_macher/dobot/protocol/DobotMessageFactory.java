package com.die_macher.dobot.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

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
     * Creates a SetPTPCmd command message.
     *
     * @param x        The target x-coordinate in Cartesian space (in millimeters).
     * @param y        The target y-coordinate in Cartesian space (in millimeters).
     * @param z        The target z-coordinate in Cartesian space (in millimeters).
     * @param r        The target rotation around the z-axis (in degrees).
     * @param isQueued Whether the command should be queued for later execution.
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetPTPCmdMessage(float x, float y, float z, float r, boolean isQueued) {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + Payload(17) + Checksum(1)
        byte[] message = new byte[22];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (19 bytes for command ID + control byte + ptp mode + x(4) + y(4) + z(4) + r(4), little endian)
        message[2] = 0x13;

        // Command ID (Set PTP Cmd = 84, little endian)
        message[3] = 0x54;

        message[4] = DobotProtocol.ControlBits.createControlByte(true, isQueued);

        // Ptp Mode (MOVEL_XYZ = 2, (x,y,z,r) is the target point in Cartesian coordinate system)
        message[5] = 0x02;

        // Convert float x to bytes (little endian)
        ByteBuffer.wrap(message, 6, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(x);

        // Convert float y to bytes (little endian)
        ByteBuffer.wrap(message, 10, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(y);

        // Convert float z to bytes (little endian)
        ByteBuffer.wrap(message, 14, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(z);

        // Convert float r to bytes (little endian)
        ByteBuffer.wrap(message, 18, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(r);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a SetHOMECmd command message.
     *
     * @param isQueued Whether the command should be queued for later execution.
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetHomeCmdMessage(boolean isQueued) {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + Payload(4) + Checksum(1)
        byte[] message = new byte[9];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (6 bytes for command ID + control byte + uint32_T reserved(4), little endian)
        message[2] = 0x06;

        // Command ID (Set HOME Cmd = 31, little endian)
        message[3] = 0x1F;

        message[4] = DobotProtocol.ControlBits.createControlByte(true, isQueued);

        // Payload (reserved 4 bytes, should be zeros)
        ByteBuffer.wrap(message, 5,  4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(0);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a SetEndEffectorSuctionCup command message.
     *
     * @param isSucked Whether the suction cup should be active (suction on) or inactive (suction off).
     * @param isQueued Whether the command should be queued for later execution.
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetEndEffectorSuctionCupMessage(boolean isSucked, boolean isQueued) {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + Payload(2) + Checksum(1)
        byte[] message = new byte[7];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (4 bytes for command ID + control byte + isCtrlEnabled + isSucked, little endian)
        message[2] = 0x04;

        // Command ID (Set HOME Cmd = 31, little endian)
        message[3] = 0x3E;

        message[4] = DobotProtocol.ControlBits.createControlByte(true, isQueued);

        // Payload:
        // Byte 5: isCtrlEnabled - If true (1), the suction cup is controlled; if false (0), it's ignored.
        // Byte 6: isSucked - If true (1), suction is turned on; if false (0), suction is turned off.
        message[5] = (byte) (isSucked ? 1 : 0);
        message[6] = (byte) (isSucked ? 1 : 0);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a SetQueuedCmdStartExec command message.
     *
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetQueuedCmdStartExec() {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + [Payload] + Checksum(1)
        byte[] message = new byte[5];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (2 bytes for command ID + control byte, little endian)
        message[2] = 0x02;

        // Command ID (Set HOME Cmd = 240, little endian)
        message[3] = (byte) 0xF0;

        message[4] = DobotProtocol.ControlBits.createControlByte(true, false);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a SetDefaultHomeParams command message.
     *
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetDefaultHomeParams() {
        // Default HOME position: (0°, 45°, 45°, 0°) → Converted to radians for the Dobot
        float x = 0.0f;      // 0°
        float y = 45.0f;     // 45°
        float z = 45.0f;     // 45°
        float r = 0.0f;      // 0°

        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + Payload(16) + Checksum(1)
        byte[] message = new byte[21];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (2 bytes for command ID + control byte + x(4) + y(4) + z(4) + r(4), little endian)
        message[2] = 0x02;

        // Command ID (Set HOME Params = 30, little endian)
        message[3] = 0x1E;

        message[4] = DobotProtocol.ControlBits.createControlByte(true, false);

        // Convert float x to bytes (little endian)
        ByteBuffer.wrap(message, 5, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(x);

        // Convert float y to bytes (little endian)
        ByteBuffer.wrap(message, 9, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(y);

        // Convert float z to bytes (little endian)
        ByteBuffer.wrap(message, 13, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(z);

        // Convert float r to bytes (little endian)
        ByteBuffer.wrap(message, 17, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(r);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a SetPTPCoordinateParams command message according to protocol specifications.
     *
     * @param xyzVelocity     The velocity of xyz coordinate (mm/s)
     * @param rVelocity       The velocity of end-effector (°/s)
     * @param xyzAcceleration The acceleration of xyz coordinate (mm/s²)
     * @param rAcceleration   The acceleration of end-effector (°/s²)
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetPTPCoordinateParamsMessage(float xyzVelocity, float rVelocity, 
                                                           float xyzAcceleration, float rAcceleration) {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + Payload(16) + Checksum(1)
        byte[] message = new byte[21];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (18 bytes for command ID + control byte + 4 float parameters, little endian)
        message[2] = 0x12;

        // Command ID (Set PTP Coordinate Params = 81, little endian)
        message[3] = 0x51;

        // Control byte (Write operation, not queued)
        message[4] = DobotProtocol.ControlBits.createControlByte(true, false);

        // Convert float xyzVelocity to bytes (little endian)
        ByteBuffer.wrap(message, 5, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(xyzVelocity);

        // Convert float rVelocity to bytes (little endian)
        ByteBuffer.wrap(message, 9, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(rVelocity);

        // Convert float xyzAcceleration to bytes (little endian)
        ByteBuffer.wrap(message, 13, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(xyzAcceleration);

        // Convert float rAcceleration to bytes (little endian)
        ByteBuffer.wrap(message, 17, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putFloat(rAcceleration);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a GetPTPCoordinateParams command message according to protocol specifications.
     *
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createGetPTPCoordinateParamsMessage() {
        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + [Payload] + Checksum(1)
        byte[] message = new byte[5]; // No payload for this command

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (2 bytes for command ID + control byte, little endian)
        message[2] = 0x02;

        // Command ID (Get PTP Coordinate Params = 81, little endian)
        message[3] = 0x51;

        // Control byte (Read operation, not queued)
        message[4] = DobotProtocol.ControlBits.createControlByte(true, false);

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }

    /**
     * Creates a SetDeviceName command message.
     *
     * @param deviceName The new name to set for the device
     * @return byte array containing the complete message including checksum
     */
    public static byte[] createSetDeviceNameMessage(String deviceName) {
        // Convert device name to bytes
        byte[] deviceNameBytes = deviceName.getBytes(StandardCharsets.UTF_8);

        // Command structure: Header(2) + Length(1) + CmdID(1) + Control(1) + Payload(variable) + Checksum(1)
        byte[] message = new byte[5 + deviceNameBytes.length];

        // Header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Length (2 bytes for command ID + control byte + payload length, little endian)
        message[2] = (byte)(2 + deviceNameBytes.length);

        // Command ID (Set Device Name = 1, little endian)
        message[3] = 0x01;

        // Control byte (Write operation, not queued)
        message[4] = DobotProtocol.ControlBits.createControlByte(true, false);

        // Copy device name bytes to payload
        System.arraycopy(deviceNameBytes, 0, message, 5, deviceNameBytes.length);

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
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_DEVICE_NAME)) {
            return null;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        if (payload == null || payload.length == 0) {
            return null;
        }

        return new String(payload, StandardCharsets.UTF_8);
    }

    /**
     * Utility method to parse success state from response of SetDeviceName.
     *
     * @param response the raw response from the Dobot
     * @return true if the command was successful, false otherwise
     */
    public static boolean parseSetDeviceNameFromResponse(byte[] response) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_DEVICE_NAME)) {
            return false;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null;
    }

    /**
     * Utility method to parse success state from response of PTPCmd.
     *
     * @param response the raw response from the Dobot
     * @return the device name as a string, or null if parsing failed
     */
    public static boolean parsePTPCmdFromResponse(byte[] response, boolean isQueued) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_PTP_CMD)) {
            return false;
        }

        if (!isQueued) {
            return true;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null && payload.length != 0;
    }

    /**
     * Utility method to parse success state from response of HOMECmd.
     *
     * @param response the raw response from the Dobot
     * @return the device name as a string, or null if parsing failed
     */
    public static boolean parseHomeCmdFromResponse(byte[] response, boolean isQueued) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_HOME_CMD)) {
            return false;
        }

        if (!isQueued) {
            return true;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null && payload.length != 0;
    }

    /**
     * Utility method to parse success state from response of EndEffectorSuctionCup.
     *
     * @param response the raw response from the Dobot
     * @return the device name as a string, or null if parsing failed
     */
    public static boolean parseEndEffectorSuctionCupFromResponse(byte[] response, boolean isQueued) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_END_EFFECTOR_SUCTION_CUP)) {
            return false;
        }

        if (!isQueued) {
            return true;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null && payload.length != 0;
    }

    /**
     * Utility method to parse success state from response of QueuedCmdStartExec.
     *
     * @param response the raw response from the Dobot
     * @return the device name as a string, or null if parsing failed
     */
    public static boolean parseQueuedCmdStartExecFromResponse(byte[] response) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_QUEUED_CMD_START_EXEC)) {
            return false;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null && payload.length != 0;
    }

    /**
     * Utility method to parse success state from response of HomeParams.
     *
     * @param response the raw response from the Dobot
     * @return the device name as a string, or null if parsing failed
     */
    public static boolean parseHomeParamsFromResponse(byte[] response) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_HOME_PARAMS)) {
            return false;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null && payload.length != 0;
    }
    
    /**
     * Utility method to parse success state from response of PTPCoordinateParams.
     *
     * @param response the raw response from the Dobot
     * @return true if successful, false otherwise
     */
    public static boolean parsePTPCoordinateParamsFromResponse(byte[] response) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_PTP_COORDINATE_PARAMS)) {
            return false;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        return payload != null && payload.length != 0;
    }
    
    /**
     * Utility method to parse PTP coordinate parameters from response.
     *
     * @param response the raw response from the Dobot
     * @return array of float values [xyzVelocity, rVelocity, xyzAcceleration, rAcceleration] or null if parsing failed
     */
    public static float[] parseGetPTPCoordinateParamsFromResponse(byte[] response) {
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_PTP_COORDINATE_PARAMS)) {
            return null;
        }

        byte[] payload = DobotProtocol.extractResponsePayload(response);
        if (payload == null || payload.length < 16) { // 4 float values = 16 bytes
            return null;
        }

        float[] params = new float[4];
        ByteBuffer buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
        
        params[0] = buffer.getFloat(0);  // xyzVelocity
        params[1] = buffer.getFloat(4);  // rVelocity
        params[2] = buffer.getFloat(8);  // xyzAcceleration
        params[3] = buffer.getFloat(12); // rAcceleration
        
        return params;
    }
}