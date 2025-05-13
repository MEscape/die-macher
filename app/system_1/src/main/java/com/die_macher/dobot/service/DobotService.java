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
     * Uses the GetDeviceSN command to test communication.
     *
     * @return true if Dobot responds correctly, false otherwise
     */
    boolean pingDobot();

    /**
     * Gets the Dobot device name.
     *
     * @return the device name as a string, or null if command fails
     */
    String getDeviceName();
}