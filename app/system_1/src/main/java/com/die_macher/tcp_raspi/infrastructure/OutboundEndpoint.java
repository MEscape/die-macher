package com.die_macher.tcp_raspi.infrastructure;

import com.die_macher.pick_and_place.event.api.ImageRequestedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OutboundEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(OutboundEndpoint.class);
  private static final byte[] IMAGE_REQUEST_COMMAND = "SEND_IMAGE".getBytes();

  private final TcpSendingMessageHandler tcpSendingMessageHandler;

  @Autowired
  public OutboundEndpoint(TcpSendingMessageHandler tcpSendingMessageHandler) {
    this.tcpSendingMessageHandler = tcpSendingMessageHandler;
  }

  @EventListener
  public void requestImage(ImageRequestedEvent event) {
    LOGGER.info("Requesting image for cube {}", event.getCubeId());

    try {
      tcpSendingMessageHandler.handleMessage(
          MessageBuilder.withPayload(IMAGE_REQUEST_COMMAND).build());
      LOGGER.debug("Image request sent successfully for cube {}", event.getCubeId());
    } catch (Exception e) {
      LOGGER.error("Failed to send image request for cube {}", event.getCubeId(), e);
      throw new RuntimeException("Image request failed", e);
    }
  }
}
