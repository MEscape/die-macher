package com.die_macher.application.tcp_client;

import com.die_macher.application.config.TcpClientConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Configuration
public class TcpConnectionRegistry {

  @Autowired(required = false)
  private TcpClientConfig.TcpClientGateway tcpClientGateway;

  private final TcpConnectionManager connectionManager = new TcpConnectionManager();

  public boolean hasActiveConnections() {
    return connectionManager.hasActiveConnections();
  }

  public int getConnectionCount() {
    return connectionManager.getConnectionCount();
  }

  public String getConnectionStatus() {
    if (!hasActiveConnections()) {
      return "DISCONNECTED";
    } else if (tcpClientGateway == null) {
      return "GATEWAY_UNAVAILABLE";
    } else {
      return "CONNECTED (" + getConnectionCount() + " connections)";
    }
  }

  public boolean isReady() {
    return tcpClientGateway != null && hasActiveConnections();
  }
}
