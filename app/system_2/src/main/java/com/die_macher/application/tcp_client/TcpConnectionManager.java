package com.die_macher.application.tcp_client;

import org.springframework.integration.ip.tcp.connection.TcpConnection;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe manager for tracking open TCP client connections.
 */
public class TcpConnectionManager {

    private final Set<TcpConnection> connections = ConcurrentHashMap.newKeySet();

    public void addConnection(TcpConnection connection) {
        connections.add(connection);
    }

    public boolean removeConnection(TcpConnection connection) {
        return connections.remove(connection);
    }

    public boolean hasActiveConnections() {
        return !connections.isEmpty();
    }

    public int getConnectionCount() {
        return connections.size();
    }

    public Set<TcpConnection> getConnections() {
        return connections;
    }
}
