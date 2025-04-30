package com.die_macher.tcp;

import com.die_macher.common.util.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.Message;

@MessageEndpoint
public class InboundEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(InboundEndpoint.class);

    @ServiceActivator(inputChannel = "tcpChannel", requiresReply = "false")
    public void onMessage(Message<byte[]> message) {
        LOGGER.info("received message with connection id {}",
                message.getHeaders().get(IpHeaders.CONNECTION_ID));

        byte[] bytePayload = message.getPayload();

        String hexDump =  HexDump.hexDump(bytePayload);

        LOGGER.info("Received: \n{}", hexDump);
    }
}
