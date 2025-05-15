package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to get the velocity and acceleration parameters of the Cartesian coordinate axes in PTP mode.
 */
public class GetMovementConfigCommand implements DobotCommand<float[]> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetMovementConfigCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    @Override
    public float[] execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute GetPTPCoordinateParams command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createGetPTPCoordinateParamsMessage();

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send GetPTPCoordinateParams command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for GetPTPCoordinateParams command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_PTP_COORDINATE_PARAMS)) {
            throw new DobotCommunicationException("Invalid response format for GetPTPCoordinateParams command");
        }

        // Parse and return the parameters
        float[] params = DobotMessageFactory.parseGetPTPCoordinateParamsFromResponse(response);
        if (params != null) {
            LOGGER.debug("Successfully retrieved PTP coordinate parameters: xyzVelocity={}, rVelocity={}, xyzAcceleration={}, rAcceleration={}", 
                    params[0], params[1], params[2], params[3]);
            return params;
        } else {
            throw new DobotCommunicationException("Failed to parse PTP coordinate parameters from response");
        }
    }
}