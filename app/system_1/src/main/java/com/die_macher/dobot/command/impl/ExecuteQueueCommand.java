package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to get the device name from Dobot.
 */
public class ExecuteQueueCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecuteQueueCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetQueuedCmdStartExec command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetQueuedCmdStartExec();

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetQueuedCmdStartExec command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetQueuedCmdStartExec command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_QUEUED_CMD_START_EXEC)) {
            throw new DobotCommunicationException("Invalid response format for SetQueuedCmdStartExec command");
        }

        // Parse and return the device name
        boolean deviceName = DobotMessageFactory.parseQueuedCmdStartExecFromResponse(response);
        if (deviceName) {
            LOGGER.debug("Successfully executed queue command");
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse device name from response");
        }
    }
}