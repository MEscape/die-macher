package com.die_macher.dobot.command;

import com.die_macher.dobot.command.impl.GetDeviceNameCommand;
import com.die_macher.dobot.command.impl.GetDeviceSNCommand;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import org.springframework.stereotype.Component;

/**
 * Factory for creating and executing Dobot commands.
 * Encapsulates the command pattern implementation and provides
 * a convenient API for the service layer.
 */
@Component
public class DobotCommandExecutor {
    private final DobotSerialConnector connector;

    public DobotCommandExecutor(DobotSerialConnector connector) {
        this.connector = connector;
    }

    /**
     * Executes a command and returns its result.
     *
     * @param command the command to execute
     * @param <T> the type of result returned by the command
     * @return the command result
     * @throws DobotCommunicationException if communication with the device fails
     */
    private <T> T executeCommand(DobotCommand<T> command) throws DobotCommunicationException {
        return command.execute(connector);
    }

    /**
     * Gets the device serial number.
     *
     * @return the device serial number
     * @throws DobotCommunicationException if communication with the device fails
     */
    public String getDeviceSN() throws DobotCommunicationException {
        return executeCommand(new GetDeviceSNCommand());
    }

    /**
     * Gets the device name.
     *
     * @return the device name
     * @throws DobotCommunicationException if communication with the device fails
     */
    public String getDeviceName() throws DobotCommunicationException {
        return executeCommand(new GetDeviceNameCommand());
    }
}