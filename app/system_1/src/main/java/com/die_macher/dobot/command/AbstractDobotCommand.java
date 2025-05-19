package com.die_macher.dobot.command;

import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.exception.DobotCommunicationException;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDobotCommand<T> implements DobotCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDobotCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    /**
     * Creates the message to be sent to the Dobot.
     *
     * @return The complete byte array representing the command message.
     */
    protected abstract byte[] createMessage();

    /**
     * Gets the specific command type for validation.
     *
     * @return The command type for this message.
     */
    protected abstract DobotProtocol.Commands getCommandType();

    /**
     * Parses the response from the Dobot into the expected object type.
     *
     * @param response The byte array containing the Dobot's response.
     * @return The parsed object representation of the response.
     */
    protected abstract T parseResponse(byte[] response);

    /**
     * Executes the command by sending the appropriate message to the Dobot via the provided serial connector.
     * The method performs the following steps:
     * <p>
     * 1. **Connection Check**:
     *    Verifies that the connector is connected to the Dobot. If not, it throws a `DobotCommunicationException`.
     * </p>
     * <p>
     * 2. **Message Creation**:
     *    Calls the `createMessage()` method to build the command message in byte format.
     * </p>
     * <p>
     * 3. **Send Message**:
     *    Attempts to send the message using the connector's `sendData()` method.
     *    If the send operation fails, a `DobotCommunicationException` is thrown.
     * </p>
     * <p>
     * 4. **Receive Response**:
     *    Waits for a response from the Dobot using the connector's `readData()` method.
     *    If no response is received within the specified timeout, or the response is empty,
     *    it throws a `DobotCommunicationException`.
     * </p>
     * <p>
     * 5. **Validate Response Format**:
     *    Uses the `DobotProtocol.validateResponseFormat()` to confirm that the response
     *    matches the expected format for the specific command type.
     *    If the format is invalid, an exception is thrown.
     * </p>
     * <p>
     * 6. **Parse Response**:
     *    Attempts to parse the response into the expected object type `T` by invoking the `parseResponse()` method.
     * </p>
     * <p>
     * 7. **Return Result**:
     *    If successful, the parsed response is returned. If parsing fails, it logs an error and throws
     *    a `DobotCommunicationException`.
     * </p>
     * @param connector The {@link DobotSerialConnector} instance to communicate with the Dobot.
     * @return The parsed object of type {@code T} that represents the command's response.
     * @throws DobotCommunicationException if there is an error during communication, message sending,
     *         response parsing, or response validation.
     */
    @Override
    public T execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Not connected to Dobot");
        }

        byte[] command = createMessage();
        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);
        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for command");
        }

        if (!DobotProtocol.validateResponseFormat(response, getCommandType())) {
            throw new DobotCommunicationException("Invalid response format for command");
        }

        try {
            T parsedResponse = parseResponse(response);
            LOGGER.debug("Command executed successfully: {}", getCommandType());
            return parsedResponse;
        } catch (Exception e) {
            String errorMessage = "Failed to parse response for command: " + getCommandType();
            LOGGER.error(errorMessage, e);
            throw new DobotCommunicationException(errorMessage);
        }
    }
}
