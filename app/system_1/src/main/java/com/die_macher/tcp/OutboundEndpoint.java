package com.die_macher.tcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Outbound endpoint that sends scheduled requests to the server
 * to trigger image sending for processing by the InboundEndpoint.
 */
@Component
@EnableScheduling
@MessageEndpoint
public class OutboundEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundEndpoint.class);
    private static final byte[] IMAGE_REQUEST_COMMAND = "SEND_IMAGE".getBytes();
    
    private final TcpSendingMessageHandler tcpSendingMessageHandler;
    
    @Autowired
    public OutboundEndpoint(TcpSendingMessageHandler tcpSendingMessageHandler) {
        this.tcpSendingMessageHandler = tcpSendingMessageHandler;
    }
    
    /**
     * Scheduled method that runs every minute to request an image from the server.
     * The server should respond by sending an image which will be processed by the InboundEndpoint.
     */
    @Scheduled(fixedRate = 60000) // Run every minute (60000 ms)
    public void requestImage() {
        try {
            LOGGER.info("Sending image request to server");
            tcpSendingMessageHandler.handleMessage(MessageBuilder
                    .withPayload(IMAGE_REQUEST_COMMAND)
                    .build());
            LOGGER.info("Image request sent successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to send image request: {}", e.getMessage(), e);
        }
    }
}