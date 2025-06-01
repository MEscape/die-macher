package com.die_macher.opcua_client;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Model class to hold sensor data with temperature and humidity values.
 */
@Getter
public class SensorData {
    private final String id;
    private final double temperature;
    private final double humidity;
    private final LocalDateTime timestamp;
    
    public SensorData(String id, double temperature, double humidity) {
        this.id = id;
        this.temperature = temperature;
        this.humidity = humidity;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id='" + id + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", timestamp=" + timestamp +
                '}';
    }
}