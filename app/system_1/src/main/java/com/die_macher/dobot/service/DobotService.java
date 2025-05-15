package com.die_macher.dobot.service;

import com.die_macher.dobot.DobotCommunicationException;

/**
 * Interface for Dobot service operations.
 * Defines the contract for interacting with Dobot devices.
 */
public interface DobotService {

    /**
     * Connects to the Dobot device using configured properties.
     *
     * @throws DobotCommunicationException if connection fails
     */
    void connectToDobot() throws DobotCommunicationException;

    /**
     * Disconnects from the Dobot device.
     */
    void disconnectFromDobot();

    /**
     * Checks if the Dobot is connected.
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Checks if the Dobot is initialized and responding.
     *
     * @return true if initialized and responding, false otherwise
     */
    boolean isInitialized();

    /**
     * Pings the Dobot to check if it's responding.
     * This method sends a ping command to the Dobot to test its communication link by using the GetDeviceSN command.
     * If the device responds correctly, the communication is functional.
     *
     * @return true if the Dobot responds correctly, false otherwise
     */
    boolean pingDobot();

    /**
     * Gets the Dobot device name.
     * This method retrieves the name of the connected Dobot device.
     * If the device is not connected or the command fails, it will return null.
     *
     * @return the device name as a string, or null if the command fails or the device is not connected
     */
    String getDeviceName();

    /**
     * Moves the Dobot to the specified position.
     * This method commands the Dobot to move to a specified position in 3D space using X, Y, Z coordinates and a rotation angle.
     *
     * @param x X coordinate in the 3D space
     * @param y Y coordinate in the 3D space
     * @param z Z coordinate in the 3D space
     * @param r Rotation angle around the Z-axis
     * @return true if the movement command was successfully sent and executed
     */
    boolean moveToPosition(float x, float y, float z, float r);

    /**
     * Sends the robot back to its home position.
     * This method returns the Dobot to its predefined home position. It is often used to reset the robot's position
     * or prepare it for the next operation.
     *
     * @return true if the command was successfully sent and the robot reached the home position
     */
    boolean goHome();

    /**
     * Sets the vacuum state of the Dobot end effector.
     * This method controls the vacuum functionality on the Dobot's end effector (if equipped),
     * allowing the robot to pick up or release objects.
     *
     * @param isSucked true to activate the vacuum (suction), false to deactivate
     * @return true if the vacuum command was successfully sent and executed
     */
    boolean setVacuumState(boolean isSucked);

    /**
     * Executes the command queue.
     * This method processes and executes all commands that have been queued in the system.
     * It ensures that the Dobot performs the tasks in the order they were added to the queue.
     *
     * @return true if the commands in the queue were successfully executed
     */
    boolean executeQueue();

    /**
     * Sets the Dobot's home position to the default coordinates.
     * <p>
     * The default home position for the Dobot Magician is as follows:
     * - X: 0.0 (0°)
     * - Y: 45.0 (45°)
     * - Z: 45.0 (45°)
     * - R: 0.0 (0°)
     * </p>
     * This method configures the Dobot to use these coordinates as its
     * home position.
     *
     * @return true if the command to set the default home position was successfully sent
     */
    boolean setDefaultHome();

    /**
     * Sets the movement configuration for the Dobot.
     * <p>
     * This method allows you to configure the velocity and acceleration parameters
     * for the Point-to-Point (PTP) motion. These parameters define how fast and
     * smoothly the Dobot moves in the XYZ coordinates and rotates around the R axis.
     * </p>
     *
     * @param xyzVelocity The velocity for movement in the X, Y, and Z axes (mm/s).
     * @param rVelocity   The velocity for rotation around the R axis (°/s).
     * @param xyzAcceleration The acceleration for movement in the X, Y, and Z axes (mm/s²).
     * @param rAcceleration   The acceleration for rotation around the R axis (°/s²).
     * @return true if the configuration command was successfully sent; false otherwise.
     */
    boolean setMovementConfig(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration);

    /**
     * Retrieves the current movement configuration parameters from the Dobot.
     * <p>
     * This method fetches the Point-to-Point (PTP) motion parameters currently
     * configured on the Dobot. These parameters include velocity and acceleration
     * settings for XYZ movement and rotation.
     * </p>
     *
     * @return a float array containing the following values in order:
     *         [xyzVelocity, rVelocity, xyzAcceleration, rAcceleration]
     * @throws DobotCommunicationException if communication with the device fails
     */
    float[] getMovementConfig();

    /**
     * Sets the device name of the Dobot.
     *
     * @param deviceName The desired name for the Dobot device.
     *                   The name should be a non-empty string containing only alphanumeric characters,
     *                   and its length should not exceed the maximum length supported by the device.
     * @return true if the command was successfully sent and the device name was updated;
     *         false otherwise.
     * @throws DobotCommunicationException if communication with the device fails,
     *         or if there is an error while sending the command.
     */
    boolean setDeviceName(String deviceName);
}