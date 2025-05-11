package com.system_1.opcua_client;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.mockito.Mockito;

/**
 * Test configuration for OPC UA client tests.
 * Provides mock beans for external dependencies.
 */
@TestConfiguration
public class OpcuaClientTestConfig {

    @Bean
    @Primary
    public OpcuaSecurityUtils securityUtils() {
        return Mockito.mock(OpcuaSecurityUtils.class);
    }
    
    @Bean
    @Primary
    public OpcuaConnectionManager connectionManager() {
        return Mockito.mock(OpcuaConnectionManager.class);
    }
    
    @Bean
    @Primary
    public OpcuaSensorDataService sensorDataService() {
        return Mockito.mock(OpcuaSensorDataService.class);
    }
}