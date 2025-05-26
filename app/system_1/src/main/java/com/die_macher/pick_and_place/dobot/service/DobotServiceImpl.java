package com.die_macher.pick_and_place.dobot.service;

import com.die_macher.pick_and_place.dobot.command.DobotCommandExecutor;
import com.die_macher.pick_and_place.dobot.config.DobotProperties;
import com.die_macher.pick_and_place.dobot.config.DobotSerialConnector;
import com.die_macher.pick_and_place.dobot.exception.DobotCommunicationException;
import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;
import com.die_macher.pick_and_place.dobot.service.api.DobotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Service implementation for interacting with the Dobot device.
 * Manages connection, communication, and execution of commands.
 */
@Service
public class DobotServiceImpl implements DobotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DobotServiceImpl.class);

    private final DobotProperties properties;
    private final DobotSerialConnector connector;
    private final DobotCommandExecutor commandExecutor;
    private boolean isInitialized = false;

    @Autowired
    public DobotServiceImpl(DobotProperties properties, DobotSerialConnector connector) {
        this.properties = properties;
        this.connector = connector;
        this.commandExecutor = new DobotCommandExecutor(connector);
    }

    /**
     * Initializes the Dobot connection after the bean is created.
     */
    @PostConstruct
    public void initialize() {
        try {
            connectToDobot();

            if (connector.isConnected()) {
                if (pingDobot()) {
                    LOGGER.info("Dobot successfully initialized and responding on port: {}", properties.getPortName());
                    isInitialized = true;
                } else {
                    LOGGER.warn("Dobot initialized but not responding to ping on port: {}", properties.getPortName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error initializing Dobot: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleans up the connection to Dobot when the bean is destroyed.
     */
    @PreDestroy
    public void cleanup() {
        disconnectFromDobot();
    }

    /**
     * Connects to the Dobot device.
     *
     * @throws DobotCommunicationException if the connection fails.
     */
    @Override
    public void connectToDobot() throws DobotCommunicationException {
        if (connector.isConnected()) {
            LOGGER.info("Already connected to Dobot");
            return;
        }

        boolean connected = connector.connect(
                properties.getPortName(),
                properties.getTimeoutMillis()
        );

        if (!connected) {
            throw new DobotCommunicationException("Failed to connect to Dobot on port: " + properties.getPortName());
        }
    }

    /**
     * Disconnects from the Dobot device.
     */
    @Override
    public void disconnectFromDobot() {
        connector.disconnect();
        isInitialized = false;
    }

    /**
     * Checks if the Dobot is currently connected.
     *
     * @return true if connected, false otherwise.
     */
    @Override
    public boolean isConnected() {
        return connector.isConnected();
    }

    /**
     * Checks if the Dobot has been successfully initialized.
     *
     * @return true if initialized, false otherwise.
     */
    @Override
    public boolean isInitialized() {
        LOGGER.debug("Dobot initialization status: {}", isInitialized ? "Initialized" : "Not Initialized");
        return isInitialized;
    }

    /**
     * Sends a ping to Dobot to verify communication.
     *
     * @return true if the device responds, false otherwise.
     */
    @Override
    public boolean pingDobot() {
        try {
            LOGGER.debug("Pinging Dobot for serial number...");

            String deviceSN = commandExecutor.getDeviceSN();
            boolean pingSuccess = deviceSN != null && !deviceSN.isEmpty();
            LOGGER.info("Ping result: {}", pingSuccess ? "Success" : "Failed");

            return pingSuccess;
        } catch (DobotCommunicationException e) {
            LOGGER.warn("Ping failed: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves the name of the Dobot device.
     *
     * @return the device name, or null if retrieval fails.
     */
    @Override
    public String getDeviceName() {
        try {
            LOGGER.debug("Retrieving Dobot device name...");
            return commandExecutor.getDeviceName();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to get device name: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Moves the Dobot to the specified coordinates.
     *
     * @param ptpMode The ptpMode of dobot movement
     * @param x The X-coordinate.
     * @param y The Y-coordinate.
     * @param z The Z-coordinate.
     * @param r The rotation angle.
     * @return true if the movement command was injected successful, false otherwise.
     */
    @Override
    public boolean moveToPosition(PTPModes ptpMode, float x, float y, float z, float r) {
        try {
            LOGGER.debug("Moving Dobot to position: [X={}, Y={}, Z={}, R={}] with ptpMode {}", x, y, z, r, ptpMode);
            return commandExecutor.moveToPosition(ptpMode, x, y, z, r, true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to move: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean setLiftHeight(float jumpHeight, float maxHeight) {
        try {
            LOGGER.debug("Setting Dobot's relative lift height: {} with absolut max height {}", jumpHeight, maxHeight);
            return commandExecutor.setLiftHeight(jumpHeight, maxHeight, true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set lift height: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Moves the Dobot to its home position.
     *
     * @return true if goHome command was injected successful, false otherwise.
     */
    @Override
    public boolean goHome() {
        try {
            LOGGER.debug("Sending Dobot to home position...");
            return commandExecutor.goHome(true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to send Dobot to home position: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Activates or deactivates the vacuum pump.
     *
     * @param isSucked true to activate the vacuum, false to deactivate.
     * @return true if the operation was injected successful, false otherwise.
     */
    @Override
    public boolean setVacuumState(boolean isSucked) {
        try {
            LOGGER.debug("Setting vacuum state to: {}", isSucked ? "Activated" : "Deactivated");
            return commandExecutor.setVacuumState(isSucked, true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set vacuum state: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Executes the command queue on the Dobot.
     *
     * @return true if the queue command was injected successfully, false otherwise.
     */
    @Override
    public boolean executeQueue() {
        try {
            LOGGER.debug("Executing command queue on Dobot...");
            return commandExecutor.executeQueue();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to execute command queue: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sets the default home position for the Dobot.
     *
     * @return true if the default home position was injected successfully, false otherwise.
     */
    @Override
    public boolean setDefaultHome(float x, float y, float z, float r) {
        try {
            LOGGER.debug("Setting default home position for Dobot: [X={}, Y={}, Z={}, R={}]", x, y, z, r);
            return commandExecutor.setDefaultHomeCommand(x, y, z, r, true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set default home: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sets the movement configuration for the Dobot.
     *
     * @param xyzVelocity The velocity for XYZ movement.
     * @param rVelocity The velocity for rotation.
     * @param xyzAcceleration The acceleration for XYZ movement.
     * @param rAcceleration The acceleration for rotation.
     * @return true if the configuration was injected successfully, false otherwise.
     */
    @Override
    public boolean setMovementConfig(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration) {
        try {
            LOGGER.debug("Setting movement configuration: [xyzVelocity={}, rVelocity={}, xyzAcceleration={}, " +
                            "rAcceleration={}]",
                    xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);

            return commandExecutor.setMovementConfig(
                    xyzVelocity, rVelocity, xyzAcceleration,
                    rAcceleration, true
            );
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set movement configuration: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sets the device name for the Dobot.
     *
     * @param deviceName The new device name to be set.
     * @return true if the name was injected successfully, false otherwise.
     */
    @Override
    public boolean setDeviceName(String deviceName) {
        try {
            LOGGER.debug("Setting Dobot device name to: {}", deviceName);
            return commandExecutor.setDeviceName(deviceName);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set device name: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Clears the command execution queue on the Dobot.
     *
     * @return true if the queue was injected successfully, false otherwise.
     */
    @Override
    public boolean clearQueue() {
        try {
            LOGGER.debug("Clearing the Dobot command execution queue...");
            return commandExecutor.clearQueue();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to clear command execution queue: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Stops the execution of the current command queue on the Dobot.
     *
     * @return true if the queue execution was injected successfully, false otherwise.
     */
    @Override
    public boolean stopExecuteQueue() {
        try {
            LOGGER.debug("Stopping execution of the Dobot command queue...");
            return commandExecutor.stopExecuteQueue();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to stop command execution queue: {}", e.getMessage(), e);
            return false;
        }
    }
}