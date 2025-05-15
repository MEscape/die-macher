package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to set the velocity and acceleration of the Cartesian coordinate axes in PTP mode.
 */
public class SetMovementConfigCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetMovementConfigCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    private final float xyzVelocity;
    private final float rVelocity;
    private final float xyzAcceleration;
    private final float rAcceleration;

    /**
     * Constructs a new {@code SetPTPCoordinateParamsCommand} to set the velocity and acceleration parameters.
     *
     * @param xyzVelocity     The velocity of xyz coordinate (mm/s)
     * @param rVelocity       The velocity of end-effector (°/s)
     * @param xyzAcceleration The acceleration of xyz coordinate (mm/s²)
     * @param rAcceleration   The acceleration of end-effector (°/s²)
     */
    public SetMovementConfigCommand(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration) {
        this.xyzVelocity = xyzVelocity;
        this.rVelocity = rVelocity;
        this.xyzAcceleration = xyzAcceleration;
        this.rAcceleration = rAcceleration;
    }

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetPTPCoordinateParams command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetPTPCoordinateParamsMessage(
                xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetPTPCoordinateParams command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetPTPCoordinateParams command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_PTP_COORDINATE_PARAMS)) {
            throw new DobotCommunicationException("Invalid response format for SetPTPCoordinateParams command");
        }

        // Parse and return success state
        boolean success = DobotMessageFactory.parsePTPCoordinateParamsFromResponse(response);
        if (success) {
            LOGGER.debug("Successfully set PTP coordinate parameters: xyzVelocity={}, rVelocity={}, xyzAcceleration={}, rAcceleration={}",
                    xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse success state from response");
        }
    }
}