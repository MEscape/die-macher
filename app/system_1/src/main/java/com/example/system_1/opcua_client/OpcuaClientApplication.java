package com.example.system_1.opcua_client;

import jakarta.annotation.PostConstruct;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class OpcuaClientApplication {
	
	private static final String ENDPOINT_URL = "opc.tcp://localhost:4840";
	
	@PostConstruct
	public void startClient() {
		try {
			System.out.println("[OPC UA] Searching for endpoints at: " + ENDPOINT_URL);
			List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints(ENDPOINT_URL).get();
			
			EndpointDescription selected = endpoints.stream()
					.filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
					.filter(e -> e.getSecurityMode() == MessageSecurityMode.None)
					.findFirst()
					.orElseThrow(() -> new Exception("No suitable endpoint found"));
			
			OpcUaClientConfig config = OpcUaClientConfig.builder()
					.setApplicationName(LocalizedText.english("My OPC UA Client"))
					.setApplicationUri("urn:my:client")
					.setEndpoint(selected)
					.setIdentityProvider(new AnonymousProvider())
					.setRequestTimeout(uint(5000))
					.build();
			
			OpcUaClient client = OpcUaClient.create(config);
			
			CompletableFuture<UaClient> future = client.connect();
			future.get();

			// Get namespace index for "Die-Macher"
			UShort idx = client.getNamespaceTable().getIndex("Die-Macher");
			if (idx == null) throw new Exception("Namespace not found!");
			
			int namespaceIndex = 2;
			
			// Create NodeId (e.g., for "temperature")
			// Temperature NodeId (numeric identifier 7)
			NodeId tempNodeId = new NodeId(namespaceIndex, 7);
			// Humidity NodeId (numeric identifier 8)
			NodeId humNodeId = new NodeId(namespaceIndex, 8);

			// Read values
			CompletableFuture<DataValue> tempFuture = client.readValue(0, TimestampsToReturn.Both, tempNodeId);
			CompletableFuture<DataValue> humFuture = client.readValue(0, TimestampsToReturn.Both, humNodeId);
			
			System.out.println("Temp: " + tempFuture.get().getValue().getValue());
			System.out.println("Humidity: " + humFuture.get().getValue().getValue());

			
		} catch (Exception e) {
			System.err.println("[OPC UA] Connection failed: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// Helper metoda jer setRequestTimeout traži UnsignedInteger
	private static UInteger uint(int value) {
		return org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger.valueOf(value);
	}
	
}