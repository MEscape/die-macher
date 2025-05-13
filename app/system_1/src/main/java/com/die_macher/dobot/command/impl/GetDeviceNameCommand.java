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
public class GetDeviceNameCommand implements DobotCommand<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetDeviceNameCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    @Override
    public String execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute GetDeviceName command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createGetDeviceNameMessage();

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send GetDeviceName command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for GetDeviceName command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.GET_DEVICE_NAME)) {
            throw new DobotCommunicationException("Invalid response format for GetDeviceName command");
        }

        // Parse and return the device name
        String deviceName = DobotMessageFactory.parseDeviceNameFromResponse(response);
        if (deviceName != null) {
            LOGGER.debug("Successfully retrieved device name: {}", deviceName);
            return deviceName;
        } else {
            throw new DobotCommunicationException("Failed to parse device name from response");
        }
    }
}