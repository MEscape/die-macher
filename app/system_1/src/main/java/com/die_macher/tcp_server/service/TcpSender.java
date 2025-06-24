package com.die_macher.tcp_server.service;

import com.die_macher.tcp_server.config.ConnectionRepository;
import com.die_macher.tcp_server.context.FlowTypeContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class TcpSender {

    private static final Logger logger = LoggerFactory.getLogger(TcpSender.class);
    private final ConnectionRepository connectionManager;
    private final ObjectMapper objectMapper;

    public TcpSender(ConnectionRepository connectionManager, ObjectMapper objectMapper) {
        this.connectionManager = connectionManager;
        this.objectMapper = objectMapper;
    }

    public void send(Map<String, Object> message, byte flowType) {
        FlowTypeContextHolder.set(flowType);

        try {
            byte[] jsonBytes = convertToJsonBytes(message);
            Message<byte[]> content = MessageBuilder.withPayload(jsonBytes).build();

            for (TcpConnection conn : connectionManager.getAllConnections()) {
                conn.send(content);
            }

            logger.info("Message sent to {} connections", connectionManager.getAllConnections().size());

        } catch (Exception e) {
            logger.error("Error sending message", e);
        } finally {
            FlowTypeContextHolder.clear();
        }
    }

    private byte[] convertToJsonBytes(Map<String, Object> message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert message to JSON", e);
        }
    }

}
