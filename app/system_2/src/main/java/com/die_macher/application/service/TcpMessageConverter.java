package com.die_macher.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Utility class for converting between byte arrays and Map objects for TCP communication.
 * Handles JSON serialization/deserialization with proper error handling and logging.
 */
@Component
public class TcpMessageConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageConverter.class);

    private final ObjectMapper objectMapper;
    private final TypeReference<Map<String, Object>> mapTypeRef = new TypeReference<Map<String, Object>>() {};

    public TcpMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converts byte array to Map by parsing JSON.
     *
     * @param data byte array containing JSON data
     * @return parsed Map or null if conversion fails
     */
    public Map<String, Object> bytesToMap(byte[] data) {
        if (data == null || data.length == 0) {
            LOGGER.warn("Empty or null byte array received");
            return Collections.emptyMap();
        }

        try {
            String jsonString = new String(data);
            LOGGER.debug("Converting bytes to map: {}", jsonString);
            return objectMapper.readValue(jsonString, mapTypeRef);
        } catch (Exception e) {
            LOGGER.error("Failed to convert byte[] to Map: {}", e.getMessage(), e);
            LOGGER.debug("Raw bytes: {}", new String(data));
            return Collections.emptyMap();
        }
    }

    /**
     * Converts Map to byte array by serializing to JSON.
     *
     * @param map Map to convert
     * @return JSON bytes or empty array if conversion fails
     */
    public byte[] mapToBytes(Map<String, Object> map) {
        if (map == null) {
            return new byte[0];
        }

        try {
            String jsonString = objectMapper.writeValueAsString(map);
            LOGGER.debug("Converting map to bytes: {}", jsonString);
            return jsonString.getBytes();
        } catch (Exception e) {
            LOGGER.error("Failed to convert Map to byte[]: {}", e.getMessage(), e);
            return new byte[0];
        }
    }
}