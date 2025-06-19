package com.die_macher.tcp_server.events;

import com.die_macher.tcp_server.config.ConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.stereotype.Component;

@Component
public class TcpConnectionOpenEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TcpConnectionOpenEventListener.class);

    private final ConnectionRepository connectionManager;
    public TcpConnectionOpenEventListener(ConnectionRepository connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Bean
    public ApplicationListener<TcpConnectionOpenEvent> connectionOpenListener(){
        return event -> {
            TcpConnection newConnection = (TcpConnection) event.getSource();
            String connectionId = newConnection.getHostAddress();
            connectionManager.addConnection(connectionId, newConnection);
            logger.info("Connection {} has been opened", connectionId);
        };
    }

}
