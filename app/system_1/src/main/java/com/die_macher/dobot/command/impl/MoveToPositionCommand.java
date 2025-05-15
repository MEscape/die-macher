package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveToPositionCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoveToPositionCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    private final float x;
    private final float y;
    private final float z;
    private final float r;
    private final boolean isQueued;

    /**
     * Constructs a new {@code MoveCommand} to move the Dobot arm to the specified coordinates and rotation.
     *
     * @param x         the target X coordinate in millimeters
     * @param y         the target Y coordinate in millimeters
     * @param z         the target Z coordinate in millimeters
     * @param r         the target rotation (R) in degrees
     * @param isQueued  {@code true} if the command should be queued; {@code false} if it should be executed immediately
     */
    public MoveToPositionCommand(float x, float y, float z, float r, boolean isQueued) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.isQueued = isQueued;
    }

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetPTPCmd command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetPTPCmdMessage(x, y, z, r, isQueued);

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetPTPCmd command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetPTPCmd command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_PTP_CMD)) {
            throw new DobotCommunicationException("Invalid response format for SetPTPCmd command");
        }

        // Parse and return that it has finished
        boolean success = DobotMessageFactory.parsePTPCmdFromResponse(response, isQueued);
        if (success) {
            LOGGER.debug("Successfully moved to x: {}, y: {}, z: {}, r: {}, isQueued: {}", x, y, z, r, isQueued);
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse success state from response");
        }
    }
}
