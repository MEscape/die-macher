package com.die_macher.dobot.protocol;


/**
 * Constants for Dobot protocol communication.
 * Contains command IDs, control bits, and other protocol-specific constants.
 */
public final class DobotProtocol {

    // Prevent instantiation
    private DobotProtocol() {}

    // Protocol header
    public static final byte[] HEADER = new byte[] {(byte)0xAA, (byte)0xAA};

    // Command IDs
    public static final class Commands {
        public static final int GET_DEVICE_SN = 0;
        public static final int GET_DEVICE_NAME = 1;
        public static final int SET_DEVICE_NAME = 1;
        public static final int SET_HOME_PARAMS = 30;
        public static final int SET_HOME_CMD = 31;
        public static final int SET_END_EFFECTOR_SUCTION_CUP = 62;
        public static final int SET_PTP_CMD = 84;
        public static final int SET_PTP_COORDINATE_PARAMS = 81;
        public static final int GET_PTP_COORDINATE_PARAMS = 81;
        public static final int SET_QUEUED_CMD_START_EXEC = 240;
    }

    // Control bits
    public static final class ControlBits {
        public static final byte READ_WRITE = 0x01;    // 0: Read, 1: Write
        public static final byte IS_QUEUED = 0x02;     // 0: Not queued, 1: Queued command

        public static byte createControlByte(boolean isWrite, boolean isQueued) {
            byte controlByte = 0x00;
            if (isWrite) controlByte |= READ_WRITE;
            if (isQueued) controlByte |= IS_QUEUED;
            return controlByte;
        }
    }

    /**
     * Calculates the protocol checksum.
     * Sum of all bytes except header, mod 256.
     *
     * @param data the full message excluding checksum
     * @return the calculated checksum byte
     */
    public static byte calculateChecksum(byte[] data) {
        int sum = 0;

        for (int i = 3; i < data.length; i++) {
            sum += (data[i] & 0xFF);
        }

        sum = sum & 0xFF; // Keep only the lowest 8 bits

        // Two's complement (256 - sum) modulo 256
        return (byte) ((~sum + 1) & 0xFF);
    }

    /**
     * Validates if a response has the correct format according to protocol.
     *
     * @param response the full response received from Dobot
     * @param expectedCommandId the command ID that should be in the response
     * @return true if the response format is valid, false otherwise
     */
    public static boolean validateResponseFormat(byte[] response, int expectedCommandId) {
        // Check minimum length and header
        if (response.length < 6 ||
                response[0] != HEADER[0] ||
                response[1] != HEADER[1]) {
            return false;
        }

        // Check command ID
        int responseCommandId = response[3] & 0xFF;
        return responseCommandId == expectedCommandId;
    }

    /**
     * Extracts the payload from a protocol response and validates its checksum using 2's complement.
     *
     * @param response the full response received from Dobot
     * @return byte array containing just the payload if valid, or null if invalid
     */
    public static byte[] extractResponsePayload(byte[] response) {
        if (response.length < 6) {  // Header(2) + Len(1) + ID(1) + Ctrl(1) + Checksum(1)
            return null;
        }

        // The length field is the payload size + 2
        int payloadLength = (response[2] & 0xFF) - 2;

        if (payloadLength < 0 || payloadLength > response.length - 6) {
            return null;
        }

        // Compute checksum: sum of all payload bytes + the "check byte"
        int sum = 0;
        for (int i = 3; i < response.length - 1; i++) {
            sum += (response[i] & 0xFF);
        }

        // Validate checksum (it should be 0 if correct)
        if ((sum & 0xFF) != 0) {
            return null;
        }

        // Getting the payload
        byte[] payload = new byte[payloadLength];
        System.arraycopy(response, 5, payload, 0, payloadLength);

        return payload;
    }
}