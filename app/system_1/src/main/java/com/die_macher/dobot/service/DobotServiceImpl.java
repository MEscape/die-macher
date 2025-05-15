package com.die_macher.dobot.service;

import com.die_macher.dobot.command.DobotCommandExecutor;
import com.die_macher.dobot.config.DobotProperties;
import com.die_macher.dobot.config.DobotSerialConnector;
import com.die_macher.dobot.DobotCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

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

    @PreDestroy
    public void cleanup() {
        disconnectFromDobot();
    }

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

    @Override
    public void disconnectFromDobot() {
        connector.disconnect();
        isInitialized = false;
    }

    @Override
    public boolean isConnected() {
        return connector.isConnected();
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public boolean pingDobot() {
        try {
            String deviceSN = commandExecutor.getDeviceSN();
            return deviceSN != null && !deviceSN.isEmpty();
        } catch (DobotCommunicationException e) {
            LOGGER.warn("Ping failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getDeviceName() {
        try {
            return commandExecutor.getDeviceName();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to get device name: {}", e.getMessage());
            return null;
        }
    }
    
    @Override
    public boolean moveToPosition(float x, float y, float z, float r) {
        try {
            return commandExecutor.moveToPosition(x, y, z, r, true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to move: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean goHome() {
        try {
            return commandExecutor.goHome(true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to go home: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean setVacuumState(boolean isSucked) {
        try {
            return commandExecutor.setVacuumState(isSucked, true);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to change the suck state: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean executeQueue() {
        try {
            return commandExecutor.executeQueue();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to execute the queue: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean setDefaultHome() {
        try {
            return commandExecutor.setDefaultHomeCommand();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set home: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean setMovementConfig(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration) {
        try {
            return commandExecutor.setMovementConfig(xyzVelocity, rVelocity, xyzAcceleration, rAcceleration);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set new movement config: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public float[] getMovementConfig() {
        try {
            return commandExecutor.getMovementConfig();
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to retrieve movement config: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean setDeviceName(String deviceName) {
        try {
            return commandExecutor.setDeviceName(deviceName);
        } catch (DobotCommunicationException e) {
            LOGGER.error("Failed to set new device name: {}", e.getMessage());
            return false;
        }
    }
}