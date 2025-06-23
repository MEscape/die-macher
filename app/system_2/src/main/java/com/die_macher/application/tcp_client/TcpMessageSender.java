package com.die_macher.application.tcp_client;

import com.die_macher.application.service.TcpMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Sends and receives TCP messages via the TcpClientGateway.
 */
@Component
public class TcpMessageSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpMessageSender.class);

    private final TcpConnectionRegistry registry;
    private final TcpMessageConverter converter;

    public TcpMessageSender(TcpConnectionRegistry registry, TcpMessageConverter converter) {
        this.registry = registry;
        this.converter = converter;
    }

    public byte[] sendAndReceive(byte[] message) {
        try {
            Map<String, Object> messageMap = converter.bytesToMap(message);

            if (!messageMap.containsKey("correlationId")) {
                messageMap.put("correlationId", UUID.randomUUID().toString());
            }

            byte[] response = registry.getTcpClientGateway().sendAndReceive(messageMap);
            LOGGER.info("Received: {}", new String(response));
            return response;
        } catch (MessagingException e) {
            LOGGER.error("Messaging error during send/receive: {}", e.getMessage(), e);
            throw e; // Re-throw to let caller handle
        } catch (Exception e) {
            LOGGER.error("Failed to send and receive byte[]: {}", e.getMessage(), e);
            throw new RuntimeException("TCP communication failed", e);
        }
    }
}
