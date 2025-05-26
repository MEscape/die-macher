package com.die_macher.tcp_raspi.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpPropertiesTest {
    private Validator validator;

    @BeforeEach
    void setupValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "localhost",
            "127.0.0.1",
            "192.168.1.1",
            "10.0.0.1",
            "8.8.8.8",
            "0.0.0.0"
    })
    void validConfig_shouldPassValidation(String host) {
        TcpProperties props = new TcpProperties();
        props.setPort(8080);
        props.setMaxMessageSize(4096);
        props.setHeaderSize(3);
        props.setHost(host);

        Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
        assertTrue(violations.isEmpty(), "Expected no validation errors");
    }

    @Test
    void invalidPort_shouldFailValidation() {
        TcpProperties props = new TcpProperties();
        props.setPort(70000); // Too high
        props.setMaxMessageSize(4096);
        props.setHeaderSize(3);
        props.setHost("localhost");

        Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for port > 65535");

        props.setPort(0); // Too low
        violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for port < 1");
    }

    @Test
    void invalidHost_shouldFailValidation() {
        TcpProperties props = new TcpProperties();
        props.setPort(8080);
        props.setMaxMessageSize(4096);
        props.setHeaderSize(2);

        props.setHost("invalid_host");
        Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for invalid host");

        props.setHost("256.256.256.256"); // Invalid IP
        violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for invalid IP");

        props.setHost(""); // Empty
        violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for empty host");
    }

    @Test
    void validHost_shouldPassValidation() {
        TcpProperties props = new TcpProperties();
        props.setPort(8080);
        props.setMaxMessageSize(4096);
        props.setHeaderSize(2);

        String[] validHosts = { "localhost", "192.168.0.1", "10.0.0.1", "127.0.0.1" };

        for (String host : validHosts) {
            props.setHost(host);
            Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
            assertTrue(violations.isEmpty(), "Expected valid host: " + host);
        }
    }

    @Test
    void invalidHeaderSize_shouldFailValidation() {
        TcpProperties props = new TcpProperties();
        props.setPort(8080);
        props.setMaxMessageSize(4096);
        props.setHost("localhost");

        props.setHeaderSize(0); // Too low
        Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for headerSize < 1");

        props.setHeaderSize(5); // Too high
        violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for headerSize > 4");
    }

    @Test
    void validHeaderSize_shouldPassValidation() {
        TcpProperties props = new TcpProperties();
        props.setPort(8080);
        props.setMaxMessageSize(4096);
        props.setHost("localhost");

        for (int i = 1; i <= 4; i++) {
            props.setHeaderSize(i);
            Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
            assertTrue(violations.isEmpty(), "Expected valid headerSize: " + i);
        }
    }

    @Test
    void invalidMaxMessageSize_shouldFailValidation() {
        TcpProperties props = new TcpProperties();
        props.setPort(8080);
        props.setHeaderSize(2);
        props.setHost("localhost");

        props.setMaxMessageSize(512); // Too low
        Set<ConstraintViolation<TcpProperties>> violations = validator.validate(props);
        assertFalse(violations.isEmpty(), "Expected validation error for maxMessageSize < 1024");
    }
}

