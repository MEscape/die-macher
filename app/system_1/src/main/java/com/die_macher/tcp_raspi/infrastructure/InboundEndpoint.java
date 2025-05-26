package com.die_macher.tcp_raspi.infrastructure;

import com.die_macher.common.util.HexDump;
import com.die_macher.pick_and_place.event.api.ImageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.Message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@MessageEndpoint
public class InboundEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundEndpoint.class);

    private final ApplicationEventPublisher eventPublisher;
    private final AtomicInteger eventIdCounter = new AtomicInteger(1);

    @Autowired
    public InboundEndpoint(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @ServiceActivator(inputChannel = "tcpChannel", requiresReply = "false")
    public void processImageMessage(Message<byte[]> message) {
        String connectionId = (String) message.getHeaders().get(IpHeaders.CONNECTION_ID);
        LOGGER.info("Received image message from connection: {}", connectionId);

        byte[] bytePayload = message.getPayload();
        String hexDump = HexDump.hexDump(bytePayload);
        LOGGER.info("Received: \n{}", hexDump);

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytePayload));
            if (image == null) {
                LOGGER.error("Failed to parse received data as image");
                return;
            }

            int eventId = eventIdCounter.getAndIncrement();

            // Publish color detected event
            eventPublisher.publishEvent(new ImageReceivedEvent(this, image, eventId));

        } catch (IOException e) {
            LOGGER.error("Error processing image message", e);
        }
    }
}
