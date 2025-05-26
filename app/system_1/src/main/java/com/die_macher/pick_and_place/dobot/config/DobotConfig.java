package com.die_macher.pick_and_place.dobot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for Dobot components.
 * Provides beans for core components that need to be shared across the application.
 */
@Configuration
public class DobotConfig {

    /**
     * Creates a serial connector bean for communication with the Dobot device.
     *
     * @return a new DobotSerialConnector instance
     */
    @Bean
    public DobotSerialConnector dobotSerialConnector() {
        return new DobotSerialConnector();
    }
}