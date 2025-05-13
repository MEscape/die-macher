package com.die_macher.opcua_client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
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
    void testConnectToServerNoEndpoints() {
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
    void testDisconnect() {
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
    void testDisconnectWithException() {
        // Arrange
        CompletableFuture<Void> disconnectFuture = new CompletableFuture<>();
        disconnectFuture.completeExceptionally(new Exception("Disconnect error"));
        doReturn(disconnectFuture).when(opcUaClient).disconnect();

        // Set client field using reflection
        ReflectionTestUtils.setField(connectionManager, "client", opcUaClient);
        ReflectionTestUtils.setField(connectionManager, "connected", true);

        // Act
        connectionManager.disconnect();

        // Assert
        verify(opcUaClient).disconnect();
        assertFalse(connectionManager.isConnected(), "Connected flag should be false even after disconnect exception");
    }

    @Test
    void testDisconnectWhenNotConnected() {
        // Arrange
        ReflectionTestUtils.setField(connectionManager, "client", null);
        ReflectionTestUtils.setField(connectionManager, "connected", false);
        
        // Act
        connectionManager.disconnect();
        
        // Assert
        // No exception should be thrown, and the state should remain unchanged
        assertFalse(connectionManager.isConnected(), "Connected flag should remain false");
        assertNull(connectionManager.getClient(), "Client should remain null");
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
    void testReconnectFailure() {
        // Arrange
        OpcuaConnectionManager spyManager = spy(connectionManager);
        
        // Mock disconnect and connectToServer methods
        doNothing().when(spyManager).disconnect();
        doReturn(false).when(spyManager).connectToServer();
        
        // Act
        boolean result = spyManager.reconnect();
        
        // Assert
        verify(spyManager).disconnect();
        verify(spyManager).connectToServer();
        assertFalse(result, "Reconnect should return false when connectToServer fails");
    }
    
    @Test
    void testConnectToServerWithEndpointsButSecurityUtilsException() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        List<EndpointDescription> endpoints = List.of(mockEndpoint);
        CompletableFuture<List<EndpointDescription>> endpointsFuture = 
            CompletableFuture.completedFuture(endpoints);
            
        when(securityUtils.loadCertificate(anyString())).thenThrow(new Exception("Certificate loading error"));
        
        try (MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(endpointsFuture);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertFalse(result, "Connection should fail when security utils throws an exception");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
        }
    }
    
    @Test
    void testConnectToServerWithEndpointsDiscoveryException() {
        // Arrange
        CompletableFuture<List<EndpointDescription>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new Exception("Discovery error"));
        
        try (MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(failedFuture);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertFalse(result, "Connection should fail when endpoint discovery fails");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
        }
    }

/*    @Test
    void testConnectToServerClientConnectException() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        when(mockEndpoint.getSecurityPolicyUri()).thenReturn("http://opcfoundation.org/UA/SecurityPolicy#Basic256Sha256");
        when(mockEndpoint.getSecurityMode()).thenReturn(MessageSecurityMode.SignAndEncrypt);

        List<EndpointDescription> endpoints = List.of(mockEndpoint);
        CompletableFuture<List<EndpointDescription>> endpointsFuture =
                CompletableFuture.completedFuture(endpoints);

        when(securityUtils.loadCertificate(anyString())).thenReturn(mock(X509Certificate.class));
        when(securityUtils.loadPrivateKey(anyString())).thenReturn(mock(PrivateKey.class));

        CompletableFuture<OpcUaClient> connectFuture = new CompletableFuture<>();
        connectFuture.completeExceptionally(new Exception("Connect error"));

        try (MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class);
             MockedStatic<OpcUaClient> opcUaClientMock = Mockito.mockStatic(OpcUaClient.class)) {

            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                    .thenReturn(endpointsFuture);

            // Fix: Return the OpcUaClient instance directly
            opcUaClientMock.when(() -> OpcUaClient.create(any(OpcUaClientConfig.class)))
                    .thenReturn(opcUaClient);

            // This is where we set up the CompletableFuture for the connect method
            //when(opcUaClient.connect()).thenReturn(connectFuture);

            // Act
            boolean result = connectionManager.connectToServer();

            // Assert
            assertFalse(result, "Connection should fail when client connect fails");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
        }
    }*/
    
    @Test
    void testConnectToServerWithNoSuitableEndpoint() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        when(mockEndpoint.getSecurityPolicyUri()).thenReturn("http://opcfoundation.org/UA/SecurityPolicy#None");
        when(mockEndpoint.getSecurityMode()).thenReturn(MessageSecurityMode.None);
        
        List<EndpointDescription> endpoints = List.of(mockEndpoint);
        CompletableFuture<List<EndpointDescription>> endpointsFuture = 
            CompletableFuture.completedFuture(endpoints);
            
        when(securityUtils.loadCertificate(anyString())).thenReturn(mock(X509Certificate.class));
        when(securityUtils.loadPrivateKey(anyString())).thenReturn(mock(PrivateKey.class));
        
        try (MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(endpointsFuture);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertFalse(result, "Connection should fail when no suitable endpoint is found");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
        }
    }
}