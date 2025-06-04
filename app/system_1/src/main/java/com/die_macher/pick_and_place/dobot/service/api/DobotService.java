package com.die_macher.pick_and_place.dobot.service.api;

import com.die_macher.pick_and_place.dobot.exception.DobotCommunicationException;
import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;

/**
 * Interface for Dobot service operations. Defines the contract for interacting with Dobot devices.
 */
public interface DobotService {

  /**
   * Connects to the Dobot device using configured properties.
   *
   * @throws DobotCommunicationException if connection fails
   */
  void connectToDobot() throws DobotCommunicationException;

  /** Disconnects from the Dobot device. Closes the communication link and releases resources. */
  void disconnectFromDobot();

  /**
   * Checks if the Dobot is currently connected.
   *
   * @return true if connected, false otherwise
   */
  boolean isConnected();

  /**
   * Checks if the Dobot is initialized and ready for commands.
   *
   * @return true if initialized and responding, false otherwise
   */
  boolean isInitialized();

  /**
   * Pings the Dobot to verify communication. Sends a low-level command to validate the connection.
   *
   * @return true if the Dobot responds correctly, false otherwise
   */
  boolean pingDobot();

  /**
   * Retrieves the name of the connected Dobot device.
   *
   * @return the device name as a string, or null if unavailable
   */
  String getDeviceName();

  /**
   * Commands the Dobot to move to a specified 3D position.
   *
   * @param ptpMode The ptpMode of dobot movement
   * @param x X coordinate in millimeters
   * @param y Y coordinate in millimeters
   * @param z Z coordinate in millimeters
   * @param r Rotation angle in degrees
   * @return true if the movement command was successfully sent
   */
  boolean moveToPosition(PTPModes ptpMode, float x, float y, float z, float r);

  /**
   * Commands the Dobot to move back to its predefined home position.
   *
   * @return true if the robot reached the home position successfully
   */
  boolean goHome();

  /**
   * Controls the vacuum end effector of the Dobot.
   *
   * @param isSucked true to activate suction, false to release
   * @return true if the vacuum state was successfully changed
   */
  boolean setVacuumState(boolean isSucked);

  /**
   * Executes all pending commands in the Dobot's queue.
   *
   * @return true if the commands were executed successfully
   */
  boolean executeQueue();

  /**
   * Sets the Dobot's home position to the default coordinates.
   *
   * @param x X coordinate in millimeters
   * @param y Y coordinate in millimeters
   * @param z Z coordinate in millimeters
   * @param r Rotation angle in degrees
   * @return true if the home position was successfully set
   */
  boolean setDefaultHome(float x, float y, float z, float r);

  /**
   * Sets the movement configuration for the Dobot. Allows customization of velocities and
   * accelerations for precise control.
   *
   * @param xyzVelocity Velocity for XYZ movement (mm/s)
   * @param rVelocity Velocity for rotation (°/s)
   * @param xyzAcceleration Acceleration for XYZ movement (mm/s²)
   * @param rAcceleration Acceleration for rotation (°/s²)
   * @return true if the configuration was successful
   */
  boolean setMovementConfig(
      float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration);

  /**
   * Sets a custom name for the Dobot device.
   *
   * @param deviceName The desired name for the Dobot
   * @return true if the name was successfully set
   */
  boolean setDeviceName(String deviceName);

  /**
   * Clears all pending commands in the Dobot's execution queue.
   *
   * @return true if the queue was cleared successfully
   */
  boolean clearQueue();

  /**
   * Halts the execution of the current command queue.
   *
   * @return true if the execution was stopped successfully
   */
  boolean stopExecuteQueue();

  /**
   * Configures the jump trajectory parameters for point-to-point (PTP) jump movements on the Dobot.
   *
   * @param jumpHeight The vertical lift height before horizontal movement begins (in mm).
   * @param maxHeight The maximum height reached during the jump trajectory (in mm).
   * @return true if the configuration was successful
   */
  boolean setLiftHeight(float jumpHeight, float maxHeight);
}
