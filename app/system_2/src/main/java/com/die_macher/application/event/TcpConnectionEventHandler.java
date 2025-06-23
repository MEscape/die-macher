package com.die_macher.application.event;

import com.die_macher.application.tcp_client.TcpConnectionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.stereotype.Component;

/**
 * Listens for TCP connection open/close events and updates the connection manager.
 */
@Component
public class TcpConnectionEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpConnectionEventHandler.class);

    private final TcpConnectionRegistry registry;

    public TcpConnectionEventHandler(TcpConnectionRegistry registry) {
        this.registry = registry;
    }

    @EventListener
    public void handleOpen(TcpConnectionOpenEvent event) {
        TcpConnection connection = (TcpConnection) event.getSource();
        registry.getConnectionManager().addConnection(connection);
        LOGGER.info(
                "TCP client connection opened: id={}, remoteAddress={}",
                connection.getConnectionId(),
                connection.getSocketInfo());
    }

    @EventListener
    public void handleClose(TcpConnectionCloseEvent event) {
        TcpConnection connection = (TcpConnection) event.getSource();
        boolean removed = registry.getConnectionManager().removeConnection(connection);
        LOGGER.info(
                "TCP client connection closed: id={}, remoteAddress={}, wasTracked={}",
                connection.getConnectionId(),
                connection.getSocketInfo(),
                removed);
    }
}
