package com.die_macher.opcua_client;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SensorDataTest {

    @Test
    void testSensorDataConstructorAndGetters() {
        // Arrange
        String id = "sensor-123";
        double temperature = 22.5;
        double humidity = 45.7;
        
        // Act
        SensorData sensorData = new SensorData(id, temperature, humidity);
        
        // Assert
        assertEquals(id, sensorData.getId(), "ID should match the constructor parameter");
        assertEquals(temperature, sensorData.getTemperature(), 0.001, "Temperature should match the constructor parameter");
        assertEquals(humidity, sensorData.getHumidity(), 0.001, "Humidity should match the constructor parameter");
        assertNotNull(sensorData.getTimestamp(), "Timestamp should not be null");
        assertTrue(sensorData.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)), 
                "Timestamp should be before or equal to current time");
        assertTrue(sensorData.getTimestamp().isAfter(LocalDateTime.now().minusMinutes(1)), 
                "Timestamp should be recent (within the last minute)");
    }
    
    @Test
    void testToString() {
        // Arrange
        SensorData sensorData = new SensorData("test-id", 20.0, 50.0);
        
        // Act
        String result = sensorData.toString();
        
        // Assert
        assertTrue(result.contains("test-id"), "toString should contain the ID");
        assertTrue(result.contains("20.0"), "toString should contain the temperature");
        assertTrue(result.contains("50.0"), "toString should contain the humidity");
        assertTrue(result.contains("timestamp"), "toString should contain the timestamp field");
    }
}