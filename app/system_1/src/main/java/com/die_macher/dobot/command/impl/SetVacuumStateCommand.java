package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetVacuumStateCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetVacuumStateCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    private final boolean isQueued;
    private final boolean isSucked;

    /**
     * Constructs a new {@code SetVacuumStateCommand} to control the vacuum (suction cup) state of the Dobot arm.
     * This command allows toggling the suction cup on or off and optionally queues the command for sequential execution.
     *
     * @param isSucked  {@code true} if the vacuum suction should be enabled (suck);
     *                  {@code false} if it should be disabled (release).
     * @param isQueued  {@code true} if the command should be added to the queue for
     *                  sequential execution; {@code false} if it should be executed immediately.
     */
    public SetVacuumStateCommand(boolean isSucked, boolean isQueued) {
        this.isSucked = isSucked;
        this.isQueued = isQueued;
    }

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetEndEffectorSuctionCup command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetEndEffectorSuctionCupMessage(isSucked, isQueued);

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetEndEffectorSuctionCup command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetEndEffectorSuctionCup command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_END_EFFECTOR_SUCTION_CUP)) {
            throw new DobotCommunicationException("Invalid response format for SetEndEffectorSuctionCup command");
        }

        // Parse and return that it has finished
        boolean success = DobotMessageFactory.parseEndEffectorSuctionCupFromResponse(response, isQueued);
        if (success) {
            LOGGER.debug("Successfully set suction cup state to '{}', isQueued: {}", isSucked ? "Sucked" : "Released", isQueued);
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse success state from response");
        }
    }
}
