package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to get the device serial number from Dobot.
 */
public class GetDeviceSNCommand implements DobotCommand<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDeviceSNCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    @Override
    public String execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute GetDeviceSN command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createGetDeviceSNMessage();

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send GetDeviceSN command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for GetDeviceSN command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_DEVICE_SN)) {
            throw new DobotCommunicationException("Invalid response format for GetDeviceSN command");
        }

        // Parse and return the SN
        String deviceSN = DobotMessageFactory.parseDeviceSNFromResponse(response);
        if (deviceSN != null) {
            LOGGER.debug("Successfully retrieved device SN: {}", deviceSN);
            return deviceSN;
        } else {
            throw new DobotCommunicationException("Failed to parse device SN from response");
        }
    }
}