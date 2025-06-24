package com.die_macher.tcp_server.config;
import org.springframework.integration.ip.tcp.connection.TcpConnection;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ConnectionRepository {

    private final Map<String, TcpConnection> connections = new ConcurrentHashMap<>();


    public void addConnection(String ipAddress, TcpConnection tcpConnection) {
        connections.put(ipAddress, tcpConnection);
    }

    public void removeConnection(String connectionId) {
        connections.remove(connectionId);
    }

    public Collection<TcpConnection> getAllConnections() {
        return connections.values();
    }

}
