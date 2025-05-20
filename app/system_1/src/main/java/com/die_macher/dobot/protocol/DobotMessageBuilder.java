package com.die_macher.dobot.protocol;

/**
 * Builder class for creating Dobot protocol messages.
 */
public class DobotMessageBuilder {
    private DobotProtocol.Commands commandId;
    private byte controlByte;
    private byte[] payload;

    /**
     * Creates a new message builder with the specified command ID.
     *
     * @param commandId the command ID for this message
     * @return this builder instance for chaining
     */
    public static DobotMessageBuilder command(DobotProtocol.Commands commandId) {
        DobotMessageBuilder builder = new DobotMessageBuilder();
        builder.commandId = commandId;
        return builder;
    }

    /**
     * Sets the control byte flags.
     *
     * @param isWrite    whether the command is a write operation
     * @param isQueued   whether the command should be queued for execution
     * @return this builder instance for chaining
     */
    public DobotMessageBuilder control(boolean isWrite, boolean isQueued) {
        this.controlByte = DobotProtocol.ControlBits.createControlByte(isWrite, isQueued);
        return this;
    }

    /**
     * Sets the payload data.
     *
     * @param payload the payload data
     * @return this builder instance for chaining
     */
    public DobotMessageBuilder payload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Builds the complete message including header, command, control bits, payload, and checksum.
     *
     * @return byte array containing the complete message
     */
    public byte[] build() {
        // Initialize payload if null
        if (payload == null) {
            payload = new byte[0];
        }
        // Calculate the message length (command ID + control byte + payload)
        int contentLength = 2 + payload.length; // 2 = commandId + controlByte

        // Allocate buffer for message without checksum
        int messageSize = DobotProtocol.Indices.HEADER_SIZE + 1 + contentLength; // +1 for length byte
        byte[] message = new byte[messageSize];

        // Add header
        message[0] = DobotProtocol.HEADER[0];
        message[1] = DobotProtocol.HEADER[1];

        // Add length
        message[DobotProtocol.Indices.LENGTH_INDEX] = (byte) contentLength;

        // Add command ID
        message[DobotProtocol.Indices.COMMAND_INDEX] = (byte) commandId.getValue();

        // Add control byte
        message[DobotProtocol.Indices.CONTROL_INDEX] = controlByte;

        // Add payload if any
        if (payload.length > 0) {
            System.arraycopy(payload, 0, message, DobotProtocol.Indices.PAYLOAD_INDEX, payload.length);
        }

        // Calculate checksum
        byte checksum = DobotProtocol.calculateChecksum(message);

        // Create final message with checksum
        byte[] completeMessage = new byte[message.length + 1];
        System.arraycopy(message, 0, completeMessage, 0, message.length);
        completeMessage[completeMessage.length - 1] = checksum;

        return completeMessage;
    }
}
