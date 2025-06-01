package com.die_macher.opcua_client;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Main OPC UA client application component that coordinates the OPC UA client functionality.
 * This class serves as the main entry point for other components to interact with OPC UA.
 */
@Component
public class OpcuaClientApplication {
    
    private final OpcuaConnectionManager connectionManager;
    private final OpcuaSensorDataService sensorDataService;
    
    @Autowired
    public OpcuaClientApplication(OpcuaConnectionManager connectionManager, 
                                 OpcuaSensorDataService sensorDataService) {
        this.connectionManager = connectionManager;
        this.sensorDataService = sensorDataService;
    }
    
    /**
     * Initialize the OPC UA client on application startup.
     */
    @PostConstruct
    public void initialize() {
        connectionManager.connectToServer();
    }
    
    /**
     * Disconnect the client when the application shuts down.
     */
    @PreDestroy
    public void shutdown() {
        connectionManager.disconnect();
    }
    
    /**
     * Read and store current sensor data
     * @return ID of the stored data or null if reading failed
     */
    public String readAndStoreSensorData() {
        if (!connectionManager.isConnected()) {
            connectionManager.connectToServer();
        }
        
        if (connectionManager.isConnected()) {
            return sensorDataService.readAndStoreSensorData(connectionManager.getClient(), 
                                                           connectionManager.getNamespaceIndex());
        }
        
        return "";
    }
    
    /**
     * Get sensor data by ID
     */
    public Optional<SensorData> getSensorData(String id) {
        return sensorDataService.getSensorData(id);
    }
    
    /**
     * Get the latest sensor data
     */
    public Optional<SensorData> getLatestSensorData() {
        String id = readAndStoreSensorData();
        return sensorDataService.getSensorData(id);
    }
    
    /**
     * Clear all stored sensor data
     */
    public void clearSensorData() {
        sensorDataService.clearSensorData();
    }
    
    /**
     * Check if the client is connected to the server
     */
    public boolean isConnected() {
        return connectionManager.isConnected();
    }
    
    /**
     * Reconnect to the OPC UA server
     */
    public boolean reconnect() {
        return connectionManager.reconnect();
    }
}