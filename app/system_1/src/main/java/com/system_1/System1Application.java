package com.system_1;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.system_1.opcua_client.OpcuaClientApplication;
import com.system_1.opcua_client.SensorData;

@SpringBootApplication
public class System1Application {

    private final OpcuaClientApplication opcuaClient;

    @Autowired
    public System1Application(OpcuaClientApplication opcuaClient) {
        this.opcuaClient = opcuaClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(System1Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void testOpcuaClient() {
        System.out.println("Testing OPC UA Client after application is ready...");
        
        // Check connection status
        boolean isConnected = opcuaClient.isConnected();
        System.out.println("OPC UA Client connected: " + isConnected);
        
        if (!isConnected) {
            System.out.println("Attempting to connect...");
            boolean reconnected = opcuaClient.reconnect();
            System.out.println("Reconnection successful: " + reconnected);
        }
        
        // Try to read sensor data
        System.out.println("Reading sensor data...");
        Optional<SensorData> latestData = opcuaClient.getLatestSensorData();
        
        if (latestData.isPresent()) {
            SensorData data = latestData.get();
            System.out.println("ID: " + data.getId());
            System.out.println("Temperature: " + data.getTemperature());
            System.out.println("Humidity: " + data.getHumidity());
        } else {
            System.out.println("Failed to retrieve sensor data");
        }
    }
}