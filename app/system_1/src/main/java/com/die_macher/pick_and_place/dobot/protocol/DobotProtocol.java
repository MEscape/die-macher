package com.die_macher.pick_and_place.dobot.protocol;

import lombok.Getter;

/**
 * Constants for Dobot protocol communication. Contains command IDs, control bits, and other
 * protocol-specific constants.
 */
public final class DobotProtocol {

  // Prevent instantiation
  private DobotProtocol() {}

  // Protocol header
  public static final byte[] HEADER = new byte[] {(byte) 0xAA, (byte) 0xAA};

  // Message indices
  public static final class Indices {
    public static final int HEADER_SIZE = 2;
    public static final int MIN_MESSAGE_SIZE = 6;

    public static final int LENGTH_INDEX = 2;
    public static final int COMMAND_INDEX = 3;
    public static final int CONTROL_INDEX = 4;
    public static final int PAYLOAD_INDEX = 5;
  }

  // Command IDs
  @Getter
  public enum Commands {
    GET_DEVICE_SN(0),
    GET_DEVICE_NAME(1),
    SET_DEVICE_NAME(1),
    SET_HOME_PARAMS(30),
    SET_HOME_CMD(31),
    SET_END_EFFECTOR_SUCTION_CUP(62),
    SET_PTP_CMD(84),
    SET_PTP_COORDINATE_PARAMS(81),
    SET_PTP_JUMP_PARAMS(82),
    SET_QUEUED_CMD_START_EXEC(240),
    SET_QUEUED_CMD_STOP(241),
    SET_QUEUED_CMD_CLEAR(245);

    private final int value;

    Commands(int value) {
      this.value = value;
    }
  }

  // Control bits
  public static final class ControlBits {
    public static final byte READ_WRITE = 0x01; // 0: Read, 1: Write
    public static final byte IS_QUEUED = 0x02; // 0: Not queued, 1: Queued command

    public static byte createControlByte(boolean isWrite, boolean isQueued) {
      byte controlByte = 0x00;
      if (isWrite) controlByte |= READ_WRITE;
      if (isQueued) controlByte |= IS_QUEUED;
      return controlByte;
    }
  }

  /**
   * Calculates the protocol checksum. Sum of all bytes except header, mod 256.
   *
   * @param data the full message excluding checksum
   * @return the calculated checksum byte
   */
  public static byte calculateChecksum(byte[] data) {
    int sum = 0;

    // Sum all bytes after header and length
    for (int i = 3; i < data.length; i++) {
      sum += (data[i] & 0xFF);
    }

    sum = sum & 0xFF; // Keep only the lowest 8 bits

    // Two's complement (256 - sum) modulo 256
    return (byte) ((~sum + 1) & 0xFF);
  }

  /**
   * Validates the checksum of a response message.
   *
   * @param response the response byte array
   * @return true if the checksum is valid, false otherwise
   */
  public static boolean validateChecksum(byte[] response) {
    int sum = 0;

    // Sum all bytes after header and length, but before checksum
    for (int i = 3; i < response.length - 1; i++) {
      sum += (response[i] & 0xFF);
    }

    // Add the checksum byte to the sum
    sum += (response[response.length - 1] & 0xFF);

    // If the sum modulo 256 is zero, the checksum is valid
    return (sum & 0xFF) == 0;
  }

  /**
   * Validates if a response has the correct format and matches the expected command.
   *
   * @param response the response byte array
   * @param expectedCommand the command ID we expect in the response
   * @return true if the response is valid
   */
  public static boolean validateResponseFormat(byte[] response, Commands expectedCommand) {
    // Check if response has minimum size
    if (response == null || response.length < Indices.MIN_MESSAGE_SIZE) {
      return false;
    }

    // Check header
    if (response[0] != HEADER[0] || response[1] != HEADER[1]) {
      return false;
    }

    // Check command ID
    if (response[Indices.COMMAND_INDEX] != (byte) expectedCommand.getValue()) {
      return false;
    }

    return validateChecksum(response);
  }

  /**
   * Extracts the payload from a protocol response and validates its checksum using 2's complement.
   *
   * @param response the full response received from Dobot
   * @return byte array containing just the payload if valid, or null if invalid
   */
  public static byte[] extractResponsePayload(byte[] response) {
    if (response == null
        || response.length
            < Indices.MIN_MESSAGE_SIZE) { // Header(2) + Len(1) + ID(1) + Ctrl(1) + Checksum(1)
      return null;
    }

    // The length field is the payload size + 2
    int payloadLength = (response[2] & 0xFF) - 2;

    if (payloadLength < 0 || payloadLength > response.length - 6) {
      return null;
    }

    // Getting the payload
    byte[] payload = new byte[payloadLength];
    System.arraycopy(response, Indices.PAYLOAD_INDEX, payload, 0, payloadLength);

    return payload;
  }
}
