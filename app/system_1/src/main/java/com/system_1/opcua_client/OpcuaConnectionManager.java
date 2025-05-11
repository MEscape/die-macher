package com.system_1.opcua_client;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manages the connection to the OPC UA server.
 */
@Component
public class OpcuaConnectionManager {
    
    @Value("${opcua.endpoint.url}")
    private String endpointUrl;
    
    @Value("${opcua.certificate.path}")
    private String certificatePath;
    
    @Value("${opcua.privatekey.path}")
    private String privateKeyPath;
    
    @Value("${opcua.namespace}")
    private String namespace;
    
    private OpcUaClient client;
    private int namespaceIndex;
    private boolean connected = false;
    
    private final OpcuaSecurityUtils securityUtils;
    
    public OpcuaConnectionManager(OpcuaSecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }
    
    /**
     * Connect to the OPC UA server.
     * @return true if connection was successful
     */
    public boolean connectToServer() {
        try {
            System.out.println("[OPC UA] Searching for endpoints at: " + endpointUrl);
            List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(endpointUrl).get();
            
            if (endpoints.isEmpty()) {
                System.err.println("[OPC UA] No endpoints found at " + endpointUrl);
                return false;
            }
            
            logEndpoints(endpoints);
            
            Security.addProvider(new BouncyCastleProvider());
            
            // Load certificate and private key
            X509Certificate certificate = securityUtils.loadCertificate(certificatePath);
            PrivateKey privateKey = securityUtils.loadPrivateKey(privateKeyPath);
            
            // Find suitable endpoint
            EndpointDescription selected = selectEndpoint(endpoints);
            
            // Build client configuration
            OpcUaClientConfig config = buildClientConfig(certificate, privateKey, selected);
            
            // Create and connect client
            client = OpcUaClient.create(config);
            
            CompletableFuture<UaClient> future = client.connect();
            future.get();
            
            // Get namespace index
            UShort idx = client.getNamespaceTable().getIndex(namespace);
            if (idx == null) {
                throw new Exception("Namespace '" + namespace + "' not found!");
            }
            
            namespaceIndex = idx.intValue();
            connected = true;
            
            System.out.println("[OPC UA] Connected successfully to server. Namespace index: " + namespaceIndex);
            return true;
            
        } catch (Exception e) {
            System.err.println("[OPC UA] Connection failed: " + e.getMessage());
            e.printStackTrace();
            connected = false;
            return false;
        }
    }
    
    /**
     * Disconnect from the OPC UA server.
     */
    public void disconnect() {
        if (client != null) {
            try {
                client.disconnect().get();
                System.out.println("[OPC UA] Client disconnected");
                connected = false;
            } catch (Exception e) {
                System.err.println("[OPC UA] Error disconnecting client: " + e.getMessage());
            }
        }
    }
    
    /**
     * Reconnect to the OPC UA server.
     */
    public boolean reconnect() {
        disconnect();
        return connectToServer();
    }
    
    /**
     * Check if the client is connected.
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Get the OPC UA client instance.
     */
    public OpcUaClient getClient() {
        return client;
    }
    
    /**
     * Get the namespace index.
     */
    public int getNamespaceIndex() {
        return namespaceIndex;
    }
    
    // Private helper methods
    
    private void logEndpoints(List<EndpointDescription> endpoints) {
        System.out.println("=== Available endpoints ===");
        for (EndpointDescription ed : endpoints) {
            System.out.println("URL: " + ed.getEndpointUrl());
            System.out.println("Security Policy: " + ed.getSecurityPolicyUri());
            System.out.println("Security Mode: " + ed.getSecurityMode());
            System.out.println("Transport Profile: " + ed.getTransportProfileUri());
            System.out.println("------------------------------");
        }
    }
    
    private EndpointDescription selectEndpoint(List<EndpointDescription> endpoints) throws Exception {
        return endpoints.stream()
                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.Basic256Sha256.getUri()))
                .filter(e -> e.getSecurityMode() == MessageSecurityMode.SignAndEncrypt || 
                             e.getSecurityMode() == MessageSecurityMode.Sign)
                .findFirst()
                .orElseThrow(() -> new Exception("No suitable endpoint found"));
    }
    
    private OpcUaClientConfig buildClientConfig(X509Certificate certificate, PrivateKey privateKey, 
                                               EndpointDescription endpoint) {
        return OpcUaClientConfig.builder()
                .setApplicationName(LocalizedText.english("OPC UA Client"))
                .setApplicationUri("urn:secure:client")
                .setCertificate(certificate)
                .setKeyPair(new KeyPair(certificate.getPublicKey(), privateKey))
                .setEndpoint(endpoint)
                .setIdentityProvider(new AnonymousProvider())
                .setRequestTimeout(uint(5000))
                .build();
    }
    
    private static UInteger uint(int value) {
        return org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger.valueOf(value);
    }
}