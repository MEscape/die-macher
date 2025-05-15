package com.die_macher.dobot.command;

import com.die_macher.dobot.command.impl.*;
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

    /**
     * Moves the Dobot to the specified position.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param r R rotation
     * @param isQueued true if the command should be queued, false otherwise
     * @return true if the command was successfully sent
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean moveToPosition(float x, float y, float z, float r, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new MoveToPositionCommand(x, y, z, r, isQueued));
    }

    /**
     * Moves the Dobot to its home position.
     *
     * @param isQueued true if the command should be queued, false otherwise
     * @return true if the command was successfully sent
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean goHome(boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new GoHomeCommand(isQueued));
    }

    /**
     * Sets the vacuum state of the Dobot end effector.
     *
     * @param isSucked true to activate the suction, false to release it
     * @param isQueued true if the command should be queued, false otherwise
     * @return true if the command was successfully sent
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setVacuumState(boolean isSucked, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new SetVacuumStateCommand(isSucked, isQueued));
    }

    /**
     * Executes the queue of commands, which were held back by the dobot.
     *
     * @return true if the command was successfully sent
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean executeQueue() throws DobotCommunicationException {
        return executeCommand(new ExecuteQueueCommand());
    }

    /**
     * Sets the Dobot to its default home position (0°, 45°, 45°, 0°).
     *
     * @return true if the command was successfully sent and acknowledged by the Dobot
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setDefaultHomeCommand() throws DobotCommunicationException {
        return executeCommand(new SetDefaultHomeCommand());
    }
    
    /**
     * Sets the velocity and acceleration parameters for PTP (Point-to-Point) motion in Cartesian coordinate system.
     *
     * @param xyzVelocity     The velocity of xyz coordinate (mm/s)
     * @param rVelocity       The velocity of end-effector (°/s)
     * @param xyzAcceleration The acceleration of xyz coordinate (mm/s²)
     * @param rAcceleration   The acceleration of end-effector (°/s²)
     * @return true if the command was successfully sent and acknowledged by the Dobot
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setMovementConfig(float xyzVelocity, float rVelocity,
                                         float xyzAcceleration, float rAcceleration) throws DobotCommunicationException {
        return executeCommand(new SetMovementConfigCommand(xyzVelocity, rVelocity,
                                                              xyzAcceleration, rAcceleration));
    }
    
    /**
     * Gets the current velocity and acceleration parameters for PTP (Point-to-Point) motion.
     *
     * @return array of float values [xyzVelocity, rVelocity, xyzAcceleration, rAcceleration]
     * @throws DobotCommunicationException if communication with the device fails
     */
    public float[] getMovementConfig() throws DobotCommunicationException {
        return executeCommand(new GetMovementConfigCommand());
    }

    /**
     * Sets the device name of the Dobot.
     *
     * @param deviceName The desired name for the Dobot device.
     * @return true if the command was successfully sent and the device name was updated;
     *         false otherwise.
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setDeviceName(String deviceName) throws DobotCommunicationException {
        return executeCommand(new SetDeviceNameCommand(deviceName));
    }
}