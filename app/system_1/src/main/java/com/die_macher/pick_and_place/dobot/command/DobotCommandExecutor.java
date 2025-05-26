package com.die_macher.pick_and_place.dobot.command;

import com.die_macher.pick_and_place.dobot.command.impl.*;
import com.die_macher.pick_and_place.dobot.config.DobotSerialConnector;
import com.die_macher.pick_and_place.dobot.exception.DobotCommunicationException;
import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;
import org.springframework.stereotype.Component;

/**
 * Factory for creating and executing Dobot commands.
 * Encapsulates the command pattern implementation and provides
 * a convenient API for the service layer to interact with the Dobot.
 */
@Component
public class DobotCommandExecutor {
    private final DobotSerialConnector connector;

    /**
     * Constructs a new DobotCommandExecutor with the given connector.
     *
     * @param connector the serial connector for communication with the Dobot
     */
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
     * Retrieves the serial number of the connected Dobot device.
     *
     * @return the serial number of the Dobot
     * @throws DobotCommunicationException if communication with the device fails
     */
    public String getDeviceSN() throws DobotCommunicationException {
        return executeCommand(new GetDeviceSNCommand());
    }

    /**
     * Retrieves the name of the connected Dobot device.
     *
     * @return the device name
     * @throws DobotCommunicationException if communication with the device fails
     */
    public String getDeviceName() throws DobotCommunicationException {
        return executeCommand(new GetDeviceNameCommand());
    }

    /**
     * Commands the Dobot to move to the specified position.
     *
     * @param ptpMode The ptpMode of dobot movement
     * @param x         X coordinate (mm)
     * @param y         Y coordinate (mm)
     * @param z         Z coordinate (mm)
     * @param r         Rotation angle (°)
     * @param isQueued  true if the command should be queued, false otherwise
     * @return true if the command was successfully sent
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean moveToPosition(PTPModes ptpMode, float x, float y, float z, float r, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new MoveToPositionCommand(ptpMode, x, y, z, r, isQueued));
    }

    /**
     * Sets the lift (jump) parameters for point-to-point (PTP) jump movements on the Dobot.
     *
     * @param jumpHeight The fixed height to lift the arm before jumping to the target position (in mm).
     * @param maxHeight  The maximum allowable height of the jump trajectory (in mm).
     * @param isQueued  true if the command should be queued, false otherwise
     * @return true if the command was successfully sent
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setLiftHeight(float jumpHeight, float maxHeight, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new SetLiftHeightCommand(jumpHeight, maxHeight, isQueued));
    }

    /**
     * Commands the Dobot to move back to its home position.
     *
     * @param isQueued true if the command should be queued, false otherwise
     * @return true if the robot reached the home position successfully
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean goHome(boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new GoHomeCommand(isQueued));
    }

    /**
     * Activates or deactivates the vacuum end effector.
     *
     * @param isSucked true to activate suction, false to release it
     * @param isQueued true if the command should be queued, false otherwise
     * @return true if the vacuum state was successfully changed
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setVacuumState(boolean isSucked, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new SetVacuumStateCommand(isSucked, isQueued));
    }

    /**
     * Executes the queue of commands held back by the Dobot.
     *
     * @return true if the commands were successfully executed
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean executeQueue() throws DobotCommunicationException {
        return executeCommand(new ExecuteQueueCommand());
    }

    /**
     * Commands the Dobot to move to its default home position (0°, 45°, 45°, 0°).
     *
     * @param x         X coordinate (mm)
     * @param y         Y coordinate (mm)
     * @param z         Z coordinate (mm)
     * @param r         Rotation angle (°)
     * @param isQueued  true if the command should be queued, false otherwise
     * @return true if the command was successfully sent and acknowledged
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setDefaultHomeCommand(float x, float y,
                                         float z, float r, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new SetDefaultHomeCommand(x, y, z, r, isQueued));
    }

    /**
     * Sets velocity and acceleration parameters for PTP (Point-to-Point) motion.
     *
     * @param xyzVelocity             Velocity for XYZ movement (mm/s)
     * @param rVelocity               Velocity for rotation (°/s)
     * @param xyzAcceleration         Acceleration for XYZ movement (mm/s²)
     * @param rAcceleration           Acceleration for rotation (°/s²)
     * @return true if the configuration was successful
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setMovementConfig(float xyzVelocity, float rVelocity,
                                     float xyzAcceleration, float rAcceleration, boolean isQueued) throws DobotCommunicationException {
        return executeCommand(new SetMovementConfigCommand(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration, isQueued));
    }

    /**
     * Sets a custom name for the Dobot device.
     *
     * @param deviceName the desired name for the Dobot
     * @return true if the name was successfully set
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean setDeviceName(String deviceName) throws DobotCommunicationException {
        return executeCommand(new SetDeviceNameCommand(deviceName));
    }

    /**
     * Clears all pending commands in the Dobot's queue.
     *
     * @return true if the queue was cleared successfully
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean clearQueue() throws DobotCommunicationException {
        return executeCommand(new ClearQueueCommand());
    }

    /**
     * Halts the execution of the current command queue.
     *
     * @return true if the execution was stopped successfully
     * @throws DobotCommunicationException if communication with the device fails
     */
    public boolean stopExecuteQueue() throws DobotCommunicationException {
        return executeCommand(new StopExecuteQueueCommand());
    }
}