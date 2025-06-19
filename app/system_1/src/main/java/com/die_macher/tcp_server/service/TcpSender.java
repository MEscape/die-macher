package com.die_macher.tcp_server.service;

import com.die_macher.tcp_server.config.ConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TcpSender {

    private static final Logger logger = LoggerFactory.getLogger(TcpSender.class);
    private final ConnectionRepository connectionManager;

    public TcpSender(ConnectionRepository connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void send(Map<String, Object> message) {
        Message<Map<String, Object>> content = MessageBuilder.withPayload(message).build();

        for (TcpConnection tcpConnection : connectionManager.getAllConnections()) {
            try {
                tcpConnection.send(content);
                logger.info("Message sent to connection: {}", tcpConnection.getConnectionId());
            } catch (Exception e) {
                logger.error("Failed to send message to {}: {}", tcpConnection.getConnectionId(), e.getMessage());
            }
        }
    }
}
