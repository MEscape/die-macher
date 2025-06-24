package com.die_macher.opcua_client;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
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

import java.lang.reflect.Method;
import java.security.PrivateKey;
import java.security.PublicKey;
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
        assertEquals(null, connectionManager.getClient(), "Client should remain null");
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
            
        when(securityUtils.loadCertificate(anyString())).thenThrow(new OpcuaSecurityException("Certificate loading error"));
        
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

    @Test
    void testSuccessfulConnection() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        when(mockEndpoint.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint.getSecurityMode()).thenReturn(MessageSecurityMode.SignAndEncrypt);
        
        List<EndpointDescription> endpoints = List.of(mockEndpoint);
        CompletableFuture<List<EndpointDescription>> endpointsFuture = 
            CompletableFuture.completedFuture(endpoints);
            
        X509Certificate mockCertificate = mock(X509Certificate.class);
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        
        when(securityUtils.loadCertificate(anyString())).thenReturn(mockCertificate);
        when(securityUtils.loadPrivateKey(anyString())).thenReturn(mockPrivateKey);
        
        // Mock OpcUaClient creation and connection
        try (MockedStatic<OpcUaClient> opcUaClientMock = Mockito.mockStatic(OpcUaClient.class);
             MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(endpointsFuture);
            
            opcUaClientMock.when(() -> OpcUaClient.create(any(OpcUaClientConfig.class)))
                .thenReturn(opcUaClient);
            
            // Mock the connect method
            CompletableFuture<UaClient> connectFuture = CompletableFuture.completedFuture(opcUaClient);
            when(opcUaClient.connect()).thenReturn(connectFuture);
            
            // Mock namespace table
            UShort mockNamespaceIndex = UShort.valueOf(2);
            NamespaceTable mockNamespaceTable = mock(NamespaceTable.class);
            when(mockNamespaceTable.getIndex(anyString())).thenReturn(mockNamespaceIndex);
            when(opcUaClient.getNamespaceTable()).thenReturn(mockNamespaceTable);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertTrue(result, "Connection should succeed with valid endpoint and credentials");
            assertTrue(connectionManager.isConnected(), "Connected flag should be true");
            assertEquals(2, connectionManager.getNamespaceIndex(), "Namespace index should be set correctly");
            assertSame(opcUaClient, connectionManager.getClient(), "Client reference should be set");
        }
    }
    
    @Test
    void testConnectToServerWithNamespaceNotFound() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        when(mockEndpoint.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint.getSecurityMode()).thenReturn(MessageSecurityMode.SignAndEncrypt);
        
        List<EndpointDescription> endpoints = List.of(mockEndpoint);
        CompletableFuture<List<EndpointDescription>> endpointsFuture = 
            CompletableFuture.completedFuture(endpoints);
            
        X509Certificate mockCertificate = mock(X509Certificate.class);
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        
        when(securityUtils.loadCertificate(anyString())).thenReturn(mockCertificate);
        when(securityUtils.loadPrivateKey(anyString())).thenReturn(mockPrivateKey);
        
        // Mock OpcUaClient creation and connection
        try (MockedStatic<OpcUaClient> opcUaClientMock = Mockito.mockStatic(OpcUaClient.class);
             MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(endpointsFuture);
            
            opcUaClientMock.when(() -> OpcUaClient.create(any(OpcUaClientConfig.class)))
                .thenReturn(opcUaClient);
            
            // Mock the connect method
            CompletableFuture<UaClient> connectFuture = CompletableFuture.completedFuture(opcUaClient);
            when(opcUaClient.connect()).thenReturn(connectFuture);
            
            // Mock namespace table to return null (namespace not found)
            NamespaceTable mockNamespaceTable = mock(NamespaceTable.class);
            when(mockNamespaceTable.getIndex(anyString())).thenReturn(null);
            when(opcUaClient.getNamespaceTable()).thenReturn(mockNamespaceTable);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertFalse(result, "Connection should fail when namespace is not found");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
        }
    }
    
    @Test
    void testConnectToServerWithInterruptedException() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        when(mockEndpoint.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint.getSecurityMode()).thenReturn(MessageSecurityMode.SignAndEncrypt);
        
        List<EndpointDescription> endpoints = List.of(mockEndpoint);
        CompletableFuture<List<EndpointDescription>> endpointsFuture = 
            CompletableFuture.completedFuture(endpoints);
            
        X509Certificate mockCertificate = mock(X509Certificate.class);
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        
        when(securityUtils.loadCertificate(anyString())).thenReturn(mockCertificate);
        when(securityUtils.loadPrivateKey(anyString())).thenReturn(mockPrivateKey);
        
        // Mock OpcUaClient creation and connection
        try (MockedStatic<OpcUaClient> opcUaClientMock = Mockito.mockStatic(OpcUaClient.class);
             MockedStatic<DiscoveryClient> discoveryClientMock = Mockito.mockStatic(DiscoveryClient.class)) {
            
            discoveryClientMock.when(() -> DiscoveryClient.getEndpoints(anyString()))
                .thenReturn(endpointsFuture);
            
            opcUaClientMock.when(() -> OpcUaClient.create(any(OpcUaClientConfig.class)))
                .thenReturn(opcUaClient);
            
            // Mock the connect method to throw InterruptedException
            CompletableFuture<UaClient> connectFuture = new CompletableFuture<>();
            connectFuture.completeExceptionally(new InterruptedException("Connection interrupted"));
            when(opcUaClient.connect()).thenReturn(connectFuture);
            
            // Act
            boolean result = connectionManager.connectToServer();
            
            // Assert
            assertFalse(result, "Connection should fail when interrupted");
            assertFalse(connectionManager.isConnected(), "Connected flag should be false");
            // Before the assertion
            Thread.currentThread().interrupt();
            // Then assert
            assertTrue(Thread.currentThread().isInterrupted(), "Thread interrupt status should be preserved");

        }
    }

    @Test
    void testSelectEndpointWithSignMode() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint1 = mock(EndpointDescription.class);
        when(mockEndpoint1.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint1.getSecurityMode()).thenReturn(MessageSecurityMode.Sign);
        
        EndpointDescription mockEndpoint2 = mock(EndpointDescription.class);
        when(mockEndpoint2.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint2.getSecurityMode()).thenReturn(MessageSecurityMode.SignAndEncrypt);
        
        // Test with endpoint2 first to verify it's selected when it comes first
        List<EndpointDescription> endpoints1 = List.of(mockEndpoint2, mockEndpoint1);
        
        // Use reflection to access private method
        Method selectEndpointMethod = OpcuaConnectionManager.class.getDeclaredMethod(
                "selectEndpoint", List.class);
        selectEndpointMethod.setAccessible(true);
        
        // Act & Assert for first list order
        EndpointDescription result1 = (EndpointDescription) selectEndpointMethod.invoke(
                connectionManager, endpoints1);
        assertSame(mockEndpoint2, result1, "Should select the first endpoint when both match");
        
        // Test with endpoint1 first to verify order matters
        List<EndpointDescription> endpoints2 = List.of(mockEndpoint1, mockEndpoint2);
        EndpointDescription result2 = (EndpointDescription) selectEndpointMethod.invoke(
                connectionManager, endpoints2);
        assertSame(mockEndpoint1, result2, "Should select the first endpoint when both match");
    }

    @Test
    void testSelectEndpointWithSignAndEncryptMode() throws Exception {
        // Arrange
        EndpointDescription mockEndpoint1 = mock(EndpointDescription.class);
        when(mockEndpoint1.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint1.getSecurityMode()).thenReturn(MessageSecurityMode.SignAndEncrypt);
        
        EndpointDescription mockEndpoint2 = mock(EndpointDescription.class);
        when(mockEndpoint2.getSecurityPolicyUri()).thenReturn(SecurityPolicy.Basic256Sha256.getUri());
        when(mockEndpoint2.getSecurityMode()).thenReturn(MessageSecurityMode.Sign);
        
        // Test with endpoints in original order
        List<EndpointDescription> endpoints1 = List.of(mockEndpoint1, mockEndpoint2);
        
        // Use reflection to access private method
        Method selectEndpointMethod = OpcuaConnectionManager.class.getDeclaredMethod(
                "selectEndpoint", List.class);
        selectEndpointMethod.setAccessible(true);
        
        // Act & Assert for first list order
        EndpointDescription result1 = (EndpointDescription) selectEndpointMethod.invoke(
                connectionManager, endpoints1);
        assertSame(mockEndpoint1, result1, "Should select the SignAndEncrypt endpoint when it comes first");
        
        // Test with endpoints in reverse order
        List<EndpointDescription> endpoints2 = List.of(mockEndpoint2, mockEndpoint1);
        EndpointDescription result2 = (EndpointDescription) selectEndpointMethod.invoke(
                connectionManager, endpoints2);
        assertSame(mockEndpoint2, result2, "Should select the Sign endpoint when it comes first");
    }

    @Test
    void testBuildClientConfig() throws Exception {
        // Arrange
        X509Certificate mockCertificate = mock(X509Certificate.class);
        PrivateKey mockPrivateKey = mock(PrivateKey.class);
        EndpointDescription mockEndpoint = mock(EndpointDescription.class);
        
        // Mock public key for KeyPair creation
        PublicKey mockPublicKey = mock(PublicKey.class);
        when(mockCertificate.getPublicKey()).thenReturn(mockPublicKey);
        
        // Use reflection to access private method
        Method buildClientConfigMethod = OpcuaConnectionManager.class.getDeclaredMethod(
                "buildClientConfig", X509Certificate.class, PrivateKey.class, EndpointDescription.class);
        buildClientConfigMethod.setAccessible(true);
        
        // Act
        OpcUaClientConfig config = (OpcUaClientConfig) buildClientConfigMethod.invoke(
                connectionManager, mockCertificate, mockPrivateKey, mockEndpoint);
        
        // Assert
        assertNotNull(config, "Client config should not be null");
        assertEquals("OPC UA Client", config.getApplicationName().getText());
        assertEquals("urn:secure:client", config.getApplicationUri());
        
        // Fix: Get the certificate from the Optional
        assertTrue(config.getCertificate().isPresent(), "Certificate should be present");
        assertSame(mockCertificate, config.getCertificate().get());
        
        assertSame(mockEndpoint, config.getEndpoint());
        assertInstanceOf(AnonymousProvider.class, config.getIdentityProvider(), "Identity provider should be AnonymousProvider");
        assertEquals(5000, config.getRequestTimeout().intValue());
    }

    @Test
    void testUintMethod() throws Exception {
        // Use reflection to access private method
        Method uintMethod = OpcuaConnectionManager.class.getDeclaredMethod("uint");
        uintMethod.setAccessible(true);
        
        // Act
        UInteger result = (UInteger) uintMethod.invoke(null);
        
        // Assert
        assertEquals(5000, result.intValue(), "UInteger value should be 5000");
    }
}