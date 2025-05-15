package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoHomeCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoHomeCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    private final boolean isQueued;

    /**
     * Constructs a new {@code GoHomeCommand} to move the Dobot arm to its home.
     *
     * @param isQueued  {@code true} if the command should be queued; {@code false} if it should be executed immediately
     */
    public GoHomeCommand(boolean isQueued) {
        this.isQueued = isQueued;
    }

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetHOMECmd command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetHomeCmdMessage(isQueued);

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetHOMECmd command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetHOMECmd command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_HOME_CMD)) {
            throw new DobotCommunicationException("Invalid response format for SetHOMECmd command");
        }

        // Parse and return that it has finished
        boolean success = DobotMessageFactory.parseHomeCmdFromResponse(response, isQueued);
        if (success) {
            LOGGER.debug("Successfully moved to its home location, isQueued: {}", isQueued);
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse success state from response");
        }
    }
}
