package com.die_macher.opcua_client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for reading and storing sensor data from OPC UA server.
 */
@Component
public class OpcuaSensorDataService {
    private static final Logger logger = LoggerFactory.getLogger(OpcuaSensorDataService.class);
    @Value("${opcua.node.temperature:7}")
    private int temperatureNodeId;
    
    @Value("${opcua.node.humidity:8}")
    private int humidityNodeId;
    
    // Store sensor data with unique IDs
    private final Map<String, SensorData> sensorDataMap = new ConcurrentHashMap<>();
    
    private final OpcuaConnectionManager connectionManager;
    
    public OpcuaSensorDataService(OpcuaConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * Read current sensor data and store it with a unique ID
     * @return The ID of the stored sensor data or null if reading failed
     */
    public String readAndStoreSensorData(OpcUaClient client, int namespaceIndex) {
        try {
            // Create NodeIds
            NodeId tempNodeId = new NodeId(namespaceIndex, temperatureNodeId);
            NodeId humNodeId = new NodeId(namespaceIndex, humidityNodeId);

            // Read values
            CompletableFuture<DataValue> tempFuture = client.readValue(0, TimestampsToReturn.Both, tempNodeId);
            CompletableFuture<DataValue> humFuture = client.readValue(0, TimestampsToReturn.Both, humNodeId);
            
            // Get values
            double temperature = extractDoubleValue(tempFuture.get());
            double humidity = extractDoubleValue(humFuture.get());
            
            logger.info("Temp: {}", temperature);
            logger.info("Humidity: {}", humidity);
            
            // Generate unique ID and store data
            String id = UUID.randomUUID().toString();
            SensorData data = new SensorData(id, temperature, humidity);
            sensorDataMap.put(id, data);

            logger.info("[OPC UA] Stored sensor data with ID: {}", id);
            return id;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("[OPC UA] Disconnect interrupted: {}", e.getMessage());
            return "";
        }
        catch (Exception e) {
            logger.error("[OPC UA] Error reading sensor data: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * Scheduled task to read sensor data periodically (every 30 seconds)
     */
    @Scheduled(fixedRate = 50000)
    public void scheduledDataReading() {
        if (connectionManager.isConnected()) {
            String id = readAndStoreSensorData(connectionManager.getClient(), connectionManager.getNamespaceIndex());

            logger.info("[OPC UA] Scheduled reading completed. Data ID: {}", id);

        } else {
            logger.info("[OPC UA] Scheduled reading skipped - not connected");
        }
    }
    
    /**
     * Get sensor data by ID
     */
    public Optional<SensorData> getSensorData(String id) {
        return Optional.ofNullable(sensorDataMap.get(id));
    }
    
    
    /**
     * Clear all stored sensor data
     */
    public void clearSensorData() {
        sensorDataMap.clear();
        logger.info("[OPC UA] Cleared all sensor data");
    }
    
    /**
     * Extract double value from DataValue
     */
    private double extractDoubleValue(DataValue dataValue) {
        Variant variant = dataValue.getValue();
        if (variant == null || variant.getValue() == null) {
            throw new IllegalArgumentException("Null value received from OPC UA server");
        }
        
        Object value = variant.getValue();
        if (value instanceof Number) {
            return (double) value;
        } else {
            throw new IllegalArgumentException("Value is not a number: " + value);
        }
    }
}