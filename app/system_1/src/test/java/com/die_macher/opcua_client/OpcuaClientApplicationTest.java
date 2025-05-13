package com.die_macher.opcua_client;

import com.die_macher.opcua_client.OpcuaClientApplication;
import com.die_macher.opcua_client.OpcuaConnectionManager;
import com.die_macher.opcua_client.OpcuaSensorDataService;
import com.die_macher.opcua_client.SensorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpcuaClientApplicationTest {

    @Mock
    private OpcuaConnectionManager connectionManager;
    
    @Mock
    private OpcuaSensorDataService sensorDataService;
    
    @InjectMocks
    private OpcuaClientApplication clientApplication;
    
    private SensorData testSensorData;
    
    @BeforeEach
    void setUp() {
        testSensorData = new SensorData("test-id", 22.5, 45.7);
    }
    
    @Test
    void testInitialize() {
        // Act
        clientApplication.initialize();
        
        // Assert
        verify(connectionManager).connectToServer();
    }
    
    @Test
    void testShutdown() {
        // Act
        clientApplication.shutdown();
        
        // Assert
        verify(connectionManager).disconnect();
    }
    
    @Test
    void testReadAndStoreSensorDataConnected() {
        // Arrange
        when(connectionManager.isConnected()).thenReturn(true);
        when(connectionManager.getClient()).thenReturn(null); // Not used in this test
        when(connectionManager.getNamespaceIndex()).thenReturn(2);
        when(sensorDataService.readAndStoreSensorData(any(), anyInt())).thenReturn("test-id");
        
        // Act
        String id = clientApplication.readAndStoreSensorData();
        
        // Assert
        assertEquals("test-id", id, "Should return the ID from the sensor data service");
        verify(connectionManager, never()).connectToServer(); // Should not try to connect if already connected
    }
    
    @Test
    void testReadAndStoreSensorDataNotConnected() {
        // Arrange
        when(connectionManager.isConnected()).thenReturn(false).thenReturn(true);
        when(connectionManager.connectToServer()).thenReturn(true);
        when(connectionManager.getClient()).thenReturn(null); // Not used in this test
        when(connectionManager.getNamespaceIndex()).thenReturn(2);
        when(sensorDataService.readAndStoreSensorData(any(), anyInt())).thenReturn("test-id");
        
        // Act
        String id = clientApplication.readAndStoreSensorData();
        
        // Assert
        assertEquals("test-id", id, "Should return the ID from the sensor data service");
        verify(connectionManager).connectToServer(); // Should try to connect first
    }
    
    @Test
    void testReadAndStoreSensorDataConnectionFailed() {
        // Arrange
        when(connectionManager.isConnected()).thenReturn(false);
        when(connectionManager.connectToServer()).thenReturn(false);
        
        // Act
        String id = clientApplication.readAndStoreSensorData();
        
        // Assert
        assertNull(id, "Should return null when connection fails");
        verify(connectionManager).connectToServer();
        verify(sensorDataService, never()).readAndStoreSensorData(any(), anyInt());
    }
    
    @Test
    void testGetSensorData() {
        // Arrange
        when(sensorDataService.getSensorData("test-id")).thenReturn(Optional.of(testSensorData));
        
        // Act
        Optional<SensorData> result = clientApplication.getSensorData("test-id");
        
        // Assert
        assertTrue(result.isPresent(), "Should return data for existing ID");
        assertEquals(testSensorData, result.get(), "Should return the correct sensor data");
    }
    
    @Test
    void testGetLatestSensorDataSuccess() {
        // Arrange
        OpcuaClientApplication spyApp = spy(clientApplication);
        doReturn("test-id").when(spyApp).readAndStoreSensorData();
        when(sensorDataService.getSensorData("test-id")).thenReturn(Optional.of(testSensorData));
        
        // Act
        Optional<SensorData> result = spyApp.getLatestSensorData();
        
        // Assert
        assertTrue(result.isPresent(), "Should return data when reading succeeds");
        assertEquals(testSensorData, result.get(), "Should return the correct sensor data");
    }
    
    @Test
    void testGetLatestSensorDataFailure() {
        // Arrange
        OpcuaClientApplication spyApp = spy(clientApplication);
        doReturn(null).when(spyApp).readAndStoreSensorData();
        
        // Act
        Optional<SensorData> result = spyApp.getLatestSensorData();
        
        // Assert
        assertFalse(result.isPresent(), "Should return empty when reading fails");
    }
    
    @Test
    void testClearSensorData() {
        // Act
        clientApplication.clearSensorData();
        
        // Assert
        verify(sensorDataService).clearSensorData();
    }
    
    @Test
    void testIsConnected() {
        // Arrange
        when(connectionManager.isConnected()).thenReturn(true);
        
        // Act
        boolean result = clientApplication.isConnected();
        
        // Assert
        assertTrue(result, "Should return the connection status from the manager");
    }
    
    @Test
    void testReconnect() {
        // Arrange
        when(connectionManager.reconnect()).thenReturn(true);
        
        // Act
        boolean result = clientApplication.reconnect();
        
        // Assert
        assertTrue(result, "Should return the reconnect result from the manager");
    }
}