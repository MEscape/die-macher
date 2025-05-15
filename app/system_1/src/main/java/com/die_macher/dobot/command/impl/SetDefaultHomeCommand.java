package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetDefaultHomeCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetDefaultHomeCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetDefaultHOMEParams command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetDefaultHomeParams();

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetDefaultHOMEParams command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetDefaultHOMEParams command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_HOME_PARAMS)) {
            throw new DobotCommunicationException("Invalid response format for SetDefaultHOMEParams command");
        }

        // Parse and return that it has finished
        boolean success = DobotMessageFactory.parseHomeParamsFromResponse(response);
        if (success) {
            LOGGER.debug("Successfully set default home location, x: 0°, y: 45°, z: 45°, r: 0°");
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse success state from response for SetDefaultHOMEParams command");
        }
    }
}
