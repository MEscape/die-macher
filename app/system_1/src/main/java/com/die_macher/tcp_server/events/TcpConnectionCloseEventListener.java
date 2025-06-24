package com.die_macher.tcp_server.events;

import com.die_macher.tcp_server.config.ConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.ip.tcp.connection.TcpConnectionCloseEvent;
import org.springframework.stereotype.Component;

@Component
public class TcpConnectionCloseEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionCloseEventListener.class);

    private final ConnectionRepository connectionManager;
    public TcpConnectionCloseEventListener(ConnectionRepository connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Bean
    public ApplicationListener<TcpConnectionCloseEvent> connectionCloseListener(){
        return event -> {
            String connectionId = event.getConnectionId();
            connectionManager.removeConnection(connectionId);
            logger.info("Connection {} has been closed", connectionId);
        };
    }

}
