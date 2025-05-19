package com.die_macher.dobot.command;

import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.exception.DobotCommunicationException;

/**
 * Base interface for all Dobot commands.
 * Defines the common operation for executing commands on the Dobot device.
 *
 * @param <T> the type of result returned by this command
 */
public interface DobotCommand<T> {

    /**
     * Executes the command on the Dobot device using the provided connector.
     *
     * @param connector the serial connector to use for communication
     * @return the result of the command execution
     * @throws DobotCommunicationException if communication with the device fails
     */
    T execute(DobotSerialConnector connector) throws DobotCommunicationException;
}