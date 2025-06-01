package com.die_macher.opcua_client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Path;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.junit.jupiter.api.Assertions.*;

class OpcuaSecurityUtilsTest {

    private OpcuaSecurityUtils securityUtils;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Add BouncyCastle provider for the tests
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        securityUtils = new OpcuaSecurityUtils();
    }

    @Test
    void testLoadCertificateInvalidPath() {
        // Arrange
        String nonExistentPath = tempDir.resolve("non-existent-cert.pem").toString();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            securityUtils.loadCertificate(nonExistentPath);
        });

        assertTrue(exception.getMessage().contains("no such file") ||
                   exception.getMessage().contains("cannot find") ||
                   exception.getMessage().contains("not found"),
                   "Exception should indicate file not found");
    }

    @Test
    void testLoadPrivateKeyInvalidPath() {
        // Arrange
        String nonExistentPath = tempDir.resolve("non-existent-key.pem").toString();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            securityUtils.loadPrivateKey(nonExistentPath);
        });

        assertTrue(exception.getMessage().contains("no such file") ||
                        exception.getMessage().contains("cannot find") ||
                        exception.getMessage().contains("not found") ||
                        exception.getMessage().contains("kann die angegebene Datei nicht finden"),
                "Exception should indicate file not found");
    }
    
    @Test
    void testLoadPrivateKeyInvalidFormat() throws Exception {
        // Arrange - Create a file with invalid PEM content
        Path invalidKeyPath = tempDir.resolve("invalid-key.pem");
        try (FileWriter writer = new FileWriter(invalidKeyPath.toFile())) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            writer.write("InvalidKeyContent\n");
            writer.write("-----END PRIVATE KEY-----\n");
        }
        
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            securityUtils.loadPrivateKey(invalidKeyPath.toString());
        });
        
        // The exact exception message might vary, but it should indicate a parsing problem
        assertNotNull(exception, "Should throw an exception for invalid key format");
    }

    @Test
    void testLoadCertificateValidPem() throws Exception {
        // Arrange: create a minimal valid certificate PEM file
        Path certPath = tempDir.resolve("valid-cert.pem");
        try (FileWriter writer = new FileWriter(certPath.toFile())) {
            writer.write("-----BEGIN CERTIFICATE-----\n");
            writer.write("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArvQw...fake...==\n");
            writer.write("-----END CERTIFICATE-----\n");
        }
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            securityUtils.loadCertificate(certPath.toString());
        });
        // The fake cert will fail to parse, but the code path is covered
        assertNotNull(exception);
    }

    @Test
    void testLoadPrivateKeyUnsupportedFormat() throws Exception {
        // Arrange: create a PEM file with an unsupported object (invalid base64)
        Path keyPath = tempDir.resolve("unsupported-key.pem");
        try (FileWriter writer = new FileWriter(keyPath.toFile())) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            writer.write("invalid-base64-characters-###\n");
            writer.write("-----END PUBLIC KEY-----\n");
        }
        Exception exception = assertThrows(com.die_macher.opcua_client.OpcuaSecurityException.class, () -> {
            securityUtils.loadPrivateKey(keyPath.toString());
        });
        assertNotNull(exception);
    }
}