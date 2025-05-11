package com.system_1.opcua_client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpcuaConnectionManagerTest {

    @Mock
    private OpcuaSecurityUtils securityUtils;
    
    @Mock
    private OpcUaClient opcUaClient;
    
    @InjectMocks
    private OpcuaConnectionManager connectionManager;
    
    @BeforeEach
    void setUp() {
        // Set required properties using reflection
        ReflectionTestUtils.setField(connectionManager, "endpointUrl", "opc.tcp://localhost:4840");
        ReflectionTestUtils.setField(connectionManager, "certificatePath", "/path/to/cert.pem");
        ReflectionTestUtils.setField(connectionManager, "privateKeyPath", "/path/to/key.pem");
        ReflectionTestUtils.setField(connectionManager, "namespace", "Die-Macher");
    }
    
    @Test
    void testConnectToServerNoEndpoints() throws Exception {
        // Arrange
        CompletableFuture<List<EndpointDescription>> emptyEndpointsFuture = 
            CompletableFuture.completedFuture(new ArrayList<>());
            
        try (MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(emptyEndpointsFuture);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertFalse(result, "Connection should fail when no endpoints are found");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
        }
    }
    
    @Test
    void testDisconnect() throws Exception {
        // Arrange
        CompletableFuture<Void> disconnectFuture = CompletableFuture.completedFuture(null);
        doReturn(disconnectFuture).when(opcUaClient).disconnect();
        
        // Set client field using reflection
        ReflectionTestUtils.setField(connectionManager, "client", opcUaClient);
        ReflectionTestUtils.setField(connectionManager, "connected", true);
        
        // Act
        connectionManager.disconnect();
        
        // Assert
        verify(opcUaClient).disconnect();
        assertFalse(connectionManager.isConnected(), "Connected flag should be false after disconnect");
    }
    
    @Test
    void testReconnect() {
        // Arrange
        OpcuaConnectionManager spyManager = spy(connectionManager);
        
        // Mock disconnect and connectToServer methods
        doNothing().when(spyManager).disconnect();
        doReturn(true).when(spyManager).connectToServer();
        
        // Act
        boolean result = spyManager.reconnect();
        
        // Assert
        verify(spyManager).disconnect();
        verify(spyManager).connectToServer();
        assertTrue(result, "Reconnect should return the result of connectToServer");
    }
    
    @Test
    void testGetters() {
        // Arrange
        ReflectionTestUtils.setField(connectionManager, "client", opcUaClient);
        ReflectionTestUtils.setField(connectionManager, "namespaceIndex", 2);
        ReflectionTestUtils.setField(connectionManager, "connected", true);
        
        // Act & Assert
        assertEquals(opcUaClient, connectionManager.getClient(), "getClient should return the client instance");
        assertEquals(2, connectionManager.getNamespaceIndex(), "getNamespaceIndex should return the namespace index");
        assertTrue(connectionManager.isConnected(), "isConnected should return the connected flag");
    }
}