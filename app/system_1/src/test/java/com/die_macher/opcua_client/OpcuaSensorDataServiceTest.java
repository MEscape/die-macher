package com.die_macher.opcua_client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpcuaSensorDataServiceTest {

    @Mock
    private OpcuaConnectionManager connectionManager;
    
    @Mock
    private OpcUaClient opcUaClient;
    
    @InjectMocks
    private OpcuaSensorDataService sensorDataService;
    
    @BeforeEach
    void setUp() {
        // Set required properties using reflection
        ReflectionTestUtils.setField(sensorDataService, "temperatureNodeId", 7);
        ReflectionTestUtils.setField(sensorDataService, "humidityNodeId", 8);
    }
    
    @Test
    void testReadAndStoreSensorDataSuccess(){
        // Arrange
        int namespaceIndex = 2;
        
        // Mock temperature data value
        DataValue tempDataValue = mock(DataValue.class);
        Variant tempVariant = mock(Variant.class);
        when(tempVariant.getValue()).thenReturn(22.5);
        when(tempDataValue.getValue()).thenReturn(tempVariant);
        CompletableFuture<DataValue> tempFuture = CompletableFuture.completedFuture(tempDataValue);
        
        // Mock humidity data value
        DataValue humDataValue = mock(DataValue.class);
        Variant humVariant = mock(Variant.class);
        when(humVariant.getValue()).thenReturn(45.7);
        when(humDataValue.getValue()).thenReturn(humVariant);
        CompletableFuture<DataValue> humFuture = CompletableFuture.completedFuture(humDataValue);
        
        // Mock client behavior - Fix: use anyDouble() instead of anyInt()
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
            .thenReturn(tempFuture)
            .thenReturn(humFuture);
        
        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);
        
        // Assert
        assertNotNull(id, "Should return a valid ID");
        
        // Verify the data was stored correctly
        Optional<SensorData> storedData = sensorDataService.getSensorData(id);
        assertTrue(storedData.isPresent(), "Sensor data should be stored");
        assertEquals(22.5, storedData.get().getTemperature(), 0.001, "Temperature should match");
        assertEquals(45.7, storedData.get().getHumidity(), 0.001, "Humidity should match");
    }
    
    @Test
    void testReadAndStoreSensorDataException() {
        // Arrange
        int namespaceIndex = 2;
        
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
            .thenThrow(new RuntimeException("Test exception"));
        
        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);
        
        // Assert
        assertEquals("", id, "Should return null when an exception occurs");
    }
    
    @Test
    void testScheduledDataReadingConnected() {
        // Arrange
        when(connectionManager.isConnected()).thenReturn(true);
        when(connectionManager.getClient()).thenReturn(opcUaClient);
        when(connectionManager.getNamespaceIndex()).thenReturn(2);
        
        // Mock the readAndStoreSensorData method
        OpcuaSensorDataService spyService = spy(sensorDataService);
        doReturn("test-id").when(spyService).readAndStoreSensorData(any(), anyInt());
        
        // Act
        spyService.scheduledDataReading();
        
        // Assert
        verify(spyService).readAndStoreSensorData(opcUaClient, 2);
    }
    
    @Test
    void testScheduledDataReadingNotConnected() {
        // Arrange
        when(connectionManager.isConnected()).thenReturn(false);
        
        // Mock the readAndStoreSensorData method
        OpcuaSensorDataService spyService = spy(sensorDataService);
        
        // Act
        spyService.scheduledDataReading();
        
        // Assert
        verify(spyService, never()).readAndStoreSensorData(any(), anyInt());
    }
    
    @Test
    void testGetSensorData() {
        // Arrange - Store some test data
        SensorData testData = new SensorData("test-id", 20.0, 50.0);
        Map<String, SensorData> map = (Map<String, SensorData>) ReflectionTestUtils.getField(sensorDataService, "sensorDataMap");
        assert map != null;
        map.put("test-id", testData);
        
        // Act
        Optional<SensorData> result = sensorDataService.getSensorData("test-id");
        Optional<SensorData> nonExistentResult = sensorDataService.getSensorData("non-existent");
        
        // Assert
        assertTrue(result.isPresent(), "Should return data for existing ID");
        assertEquals(testData, result.get(), "Should return the correct sensor data");
        assertFalse(nonExistentResult.isPresent(), "Should return empty for non-existent ID");
    }
    
    @Test
    void testClearSensorData() {
        // Arrange - Store some test data
        SensorData testData = new SensorData("test-id", 20.0, 50.0);
        Map<String, SensorData> map = (Map<String, SensorData>) ReflectionTestUtils.getField(sensorDataService, "sensorDataMap");
        assert map != null;
        map.put("test-id", testData);
        
        // Act
        sensorDataService.clearSensorData();
        
        // Assert
        Optional<SensorData> result = sensorDataService.getSensorData("test-id");
        assertFalse(result.isPresent(), "Data should be cleared");
    }

    @Test
    void testReadAndStoreSensorDataWithInterruptedException() {
        // Arrange
        int namespaceIndex = 2;

        // Create a CompletableFuture that will be interrupted
        CompletableFuture<DataValue> interruptedFuture = new CompletableFuture<>();
        interruptedFuture.completeExceptionally(new InterruptedException("Test interruption"));

        // Mock client behavior
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
                .thenReturn(interruptedFuture);

        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);

        // Assert
        assertEquals("", id, "Should return empty string when interrupted");
        assertFalse(Thread.currentThread().isInterrupted(), "Thread should be interrupted");

    }

    @Test
    void testReadAndStoreSensorDataWithNullVariant() {
        // Arrange
        int namespaceIndex = 2;

        // Mock temperature data value with null variant
        DataValue tempDataValue = mock(DataValue.class);
        when(tempDataValue.getValue()).thenReturn(null);
        CompletableFuture<DataValue> tempFuture = CompletableFuture.completedFuture(tempDataValue);

        // Mock client behavior
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
                .thenReturn(tempFuture);

        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);

        // Assert
        assertEquals("", id, "Should return empty string when variant is null");
    }

    @Test
    void testReadAndStoreSensorDataWithNullVariantValue() {
        // Arrange
        int namespaceIndex = 2;

        // Mock temperature data value with variant containing null value
        DataValue tempDataValue = mock(DataValue.class);
        Variant tempVariant = mock(Variant.class);
        when(tempVariant.getValue()).thenReturn(null);
        when(tempDataValue.getValue()).thenReturn(tempVariant);
        CompletableFuture<DataValue> tempFuture = CompletableFuture.completedFuture(tempDataValue);

        // Mock client behavior
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
                .thenReturn(tempFuture);

        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);

        // Assert
        assertEquals("", id, "Should return empty string when variant value is null");
    }

    @Test
    void testReadAndStoreSensorDataWithNonNumberValue() {
        // Arrange
        int namespaceIndex = 2;

        // Mock temperature data value with non-number value
        DataValue tempDataValue = mock(DataValue.class);
        Variant tempVariant = mock(Variant.class);
        when(tempVariant.getValue()).thenReturn("not a number");
        when(tempDataValue.getValue()).thenReturn(tempVariant);
        CompletableFuture<DataValue> tempFuture = CompletableFuture.completedFuture(tempDataValue);

        // Mock client behavior
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
                .thenReturn(tempFuture);

        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);

        // Assert
        assertEquals("", id, "Should return empty string when value is not a number");
    }

    @Test
    void testReadAndStoreSensorDataWithExecutionException() {
        // Arrange
        int namespaceIndex = 2;

        CompletableFuture<DataValue> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new ExecutionException(new RuntimeException("Test execution exception")));

        // Mock client behavior
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
                .thenReturn(failedFuture);

        // Act
        String id = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);

        // Assert
        assertEquals("", id, "Should return empty string when execution exception occurs");
    }

    @Test
    void testMultipleSensorDataEntries() {
        // Arrange
        int namespaceIndex = 2;

        // Mock temperature and humidity data values
        DataValue dataValue = mock(DataValue.class);
        Variant variant = mock(Variant.class);
        when(variant.getValue()).thenReturn(22.5);
        when(dataValue.getValue()).thenReturn(variant);
        CompletableFuture<DataValue> future = CompletableFuture.completedFuture(dataValue);

        // Mock client behavior
        when(opcUaClient.readValue(anyDouble(), any(TimestampsToReturn.class), any(NodeId.class)))
                .thenReturn(future);

        // Act - Store multiple sensor data entries
        String id1 = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);
        String id2 = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);
        String id3 = sensorDataService.readAndStoreSensorData(opcUaClient, namespaceIndex);

        // Get the map to check its size
        Map<String, SensorData> map = (Map<String, SensorData>) ReflectionTestUtils.getField(sensorDataService, "sensorDataMap");

        // Assert
        assertNotEquals(id1, id2, "IDs should be unique");
        assertNotEquals(id2, id3, "IDs should be unique");
        assertNotEquals(id1, id3, "IDs should be unique");
        assert map != null;
        assertEquals(3, map.size(), "Map should contain 3 entries");
    }
}
