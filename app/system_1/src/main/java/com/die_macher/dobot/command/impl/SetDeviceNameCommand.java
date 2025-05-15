package com.die_macher.dobot.command.impl;

import com.die_macher.dobot.command.DobotCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import com.die_macher.dobot.protocol.DobotMessageFactory;
import com.die_macher.dobot.protocol.DobotProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to set the device name from Dobot.
 */
public class SetDeviceNameCommand implements DobotCommand<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetDeviceNameCommand.class);
    private static final int RESPONSE_TIMEOUT_MS = 200;

    private final String deviceName;

    public SetDeviceNameCommand(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public Boolean execute(DobotSerialConnector connector) throws DobotCommunicationException {
        if (!connector.isConnected()) {
            throw new DobotCommunicationException("Cannot execute SetDeviceName command: Not connected to Dobot");
        }

        byte[] command = DobotMessageFactory.createSetDeviceNameMessage(deviceName);

        if (!connector.sendData(command)) {
            throw new DobotCommunicationException("Failed to send SetDeviceName command to Dobot");
        }

        byte[] response = connector.readData(RESPONSE_TIMEOUT_MS);

        if (response == null || response.length == 0) {
            throw new DobotCommunicationException("No response received for SetDeviceName command");
        }

        // Validate the response format
        if (!DobotProtocol.validateResponseFormat(response, DobotProtocol.Commands.SET_DEVICE_NAME)) {
            throw new DobotCommunicationException("Invalid response format for SetDeviceName command");
        }

        // Parse and return the device name
        boolean success = DobotMessageFactory.parseSetDeviceNameFromResponse(response);
        if (success) {
            LOGGER.debug("Successfully changed device name to: {}", deviceName);
            return true;
        } else {
            throw new DobotCommunicationException("Failed to parse device name from response");
        }
    }
}