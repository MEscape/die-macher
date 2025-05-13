package com.die_macher.opcua_client;

import com.die_macher.opcua_client.OpcuaSecurityUtils;
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

        // System.out.println("Actual exception message: " + exception.getMessage());

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
}