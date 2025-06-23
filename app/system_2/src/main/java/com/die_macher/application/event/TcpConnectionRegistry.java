package com.die_macher.application.event;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

@Getter
@Configuration
public class TcpConnectionRegistry {

  private static final Logger LOGGER = LoggerFactory.getLogger(TcpConnectionRegistry.class);

  private final Set<TcpConnection> connections = ConcurrentHashMap.newKeySet();

  @EventListener
  public void handleOpen(TcpConnectionOpenEvent event) {
    TcpConnection connection = (TcpConnection) event.getSource();
    connections.add(connection);
    LOGGER.info(
        "TCP connection opened: id={}, remoteAddress={}",
        connection.getConnectionId(),
        connection.getSocketInfo());
  }

  @EventListener
  public void handleClose(TcpConnectionCloseEvent event) {
    TcpConnection connection = (TcpConnection) event.getSource();
    boolean removed = connections.remove(connection);
    LOGGER.info(
        "TCP connection closed: id={}, remoteAddress={}, wasTracked={}",
        connection.getConnectionId(),
        connection.getSocketInfo(),
        removed);
  }

  public void send(byte[] payload) {
    for (TcpConnection connection : connections) {
      try {
        Message<byte[]> message = MessageBuilder.withPayload(payload).build();
        connection.send(message);
      } catch (Exception e) {
        LOGGER.error(
            "Failed to send TCP message to connection {}: {}",
            connection.getConnectionId(),
            e.getMessage(),
            e);
      }
    }
  }
}
