package com.die_macher.tcp;

import com.die_macher.common.util.HexDump;
import com.die_macher.service.ColorDetectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.Message;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@MessageEndpoint
public class InboundEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundEndpoint.class);

    private final ColorDetectionService colorDetectionService;

    @Autowired
    public InboundEndpoint(ColorDetectionService colorDetectionServiceLocal) {
        this.colorDetectionService = colorDetectionServiceLocal;
    }

    @ServiceActivator(inputChannel = "tcpChannel", requiresReply = "false")
    public void onMessage(Message<byte[]> message) throws IOException {
        LOGGER.info("received message with connection id {}",
                message.getHeaders().get(IpHeaders.CONNECTION_ID));

        byte[] bytePayload = message.getPayload();
        String hexDump =  HexDump.hexDump(bytePayload);
        LOGGER.info("Received: \n{}", hexDump);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytePayload));
        String dominantColor = colorDetectionService.detectDominantColor(image);
        System.out.println(dominantColor);
    }
}
