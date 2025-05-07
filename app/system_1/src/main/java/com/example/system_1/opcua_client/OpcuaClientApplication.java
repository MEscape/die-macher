package com.example.system_1.opcua_client;

import jakarta.annotation.PostConstruct;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
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
			System.out.println("=== Available endpoints ===");
			for (EndpointDescription ed : endpoints) {
				System.out.println("URL: " + ed.getEndpointUrl());
				System.out.println("Security Policy: " + ed.getSecurityPolicyUri());
				System.out.println("Security Mode: " + ed.getSecurityMode());
				System.out.println("Transport Profile: " + ed.getTransportProfileUri());
				System.out.println("------------------------------");
			}
			
			Security.addProvider(new BouncyCastleProvider());
			
			// --- Load certificate and private key from PEM ---
			X509Certificate certificate = loadCertificate("C:/Users/MS91606/Desktop/Schulprojekt/client-cert/client-cert.pem");
			PrivateKey privateKey = loadPrivateKey("C:/Users/MS91606/Desktop/Schulprojekt/client-cert/client-key.pem");
			
			// --- Discover endpoints ---
			
			EndpointDescription selected = endpoints.stream()
					.filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.Basic256Sha256.getUri()))
					.filter(e -> e.getSecurityMode() == MessageSecurityMode.SignAndEncrypt || e.getSecurityMode() == MessageSecurityMode.Sign)
					.findFirst()
					.orElseThrow(() -> new Exception("No suitable endpoint found"));

			// --- Build client config ---
			OpcUaClientConfig config = OpcUaClientConfig.builder()
					.setApplicationName(LocalizedText.english("OPC UA Client"))
					.setApplicationUri("urn:secure:client")
					.setCertificate(certificate)
					.setKeyPair(new KeyPair(certificate.getPublicKey(), privateKey))
					.setEndpoint(selected)
					.setIdentityProvider(new AnonymousProvider())
					.setRequestTimeout(uint(5000))
					.build();
			
			
			// --- Create and connect client ---
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
	
	private static X509Certificate loadCertificate(String certPath) throws Exception {
		try (PEMParser parser = new PEMParser(new FileReader(certPath))) {
			X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
			return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
		}
	}
	
	private static PrivateKey loadPrivateKey(String keyPath) throws Exception {
		try (PEMParser parser = new PEMParser(new FileReader(keyPath))) {
			Object object = parser.readObject();
			JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
			if (object instanceof org.bouncycastle.asn1.pkcs.PrivateKeyInfo) {
				return converter.getPrivateKey((org.bouncycastle.asn1.pkcs.PrivateKeyInfo) object);
			} else {
				throw new IllegalArgumentException("Unsupported private key format in PEM file.");
			}
		}
	}
	
	// Helper metoda jer setRequestTimeout traži UnsignedInteger
	private static UInteger uint(int value) {
		return org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger.valueOf(value);
	}
	
}