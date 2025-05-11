package com.system_1.opcua_client;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Utility class for handling OPC UA security-related operations.
 */
@Component
public class OpcuaSecurityUtils {
    
    /**
     * Load X.509 certificate from PEM file.
     */
    public X509Certificate loadCertificate(String certPath) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(certPath))) {
            X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        }
    }
    
    /**
     * Load private key from PEM file.
     */
    public PrivateKey loadPrivateKey(String keyPath) throws Exception {
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
}