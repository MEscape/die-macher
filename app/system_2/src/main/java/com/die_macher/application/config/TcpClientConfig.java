package com.die_macher.application.config;

import com.die_macher.infrastructure.adapter.serializer.CustomHeaderDeserializer;
import com.die_macher.infrastructure.config.properties.TcpClientProperties;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.MapJsonSerializer;
import org.springframework.messaging.MessageChannel;

@Configuration
public class TcpClientConfig {

  private final String host;
  private final int port;

  private final TcpClientProperties tcpClientProperties;

  public TcpClientConfig(TcpClientProperties tcpClientProperties) {
    this.tcpClientProperties = tcpClientProperties;
    this.host = tcpClientProperties.getHost();
    this.port = tcpClientProperties.getPort();
  }

  @Bean
  public AbstractClientConnectionFactory clientConnectionFactory(
      CustomHeaderDeserializer deserializer) {
    TcpNetClientConnectionFactory factory = new TcpNetClientConnectionFactory(host, port);
    factory.setSingleUse(true); // Try this

    factory.setSoKeepAlive(true);
    factory.setSoTimeout(tcpClientProperties.getSoTimeout());

    factory.setSerializer(jsonSerializer());
    factory.setDeserializer(deserializer);
    return factory;
  }

  @Bean
  public MessageChannel tcpClientRequestChannel() {
    return new DirectChannel();
  }

  @Bean
  public MessageChannel tcpClientReplyChannel() {
    return new DirectChannel();
  }

  @Bean
  @ServiceActivator(inputChannel = "tcpClientRequestChannel")
  public TcpOutboundGateway tcpOutboundGateway(CustomHeaderDeserializer deserializer) {
    TcpOutboundGateway gateway = new TcpOutboundGateway();
    gateway.setConnectionFactory(clientConnectionFactory(deserializer));
    gateway.setOutputChannel(tcpClientReplyChannel());

    gateway.setRequestTimeout(30000);
    gateway.setRequiresReply(true);

    return gateway;
  }

  @MessagingGateway(defaultRequestChannel = "tcpClientRequestChannel")
  public interface TcpClientGateway {
    @Gateway(requestChannel = "tcpClientRequestChannel", replyChannel = "tcpClientReplyChannel")
    byte[] sendAndReceive(Map<String, Object> message);
  }

  @Bean
  public MapJsonSerializer jsonSerializer() {
    MapJsonSerializer serializer = new MapJsonSerializer();
    ByteArrayLengthHeaderSerializer packetSerializer = new ByteArrayLengthHeaderSerializer(4);
    packetSerializer.setMaxMessageSize(tcpClientProperties.getMaxMessageSize());
    serializer.setPacketSerializer(packetSerializer);
    serializer.setPacketDeserializer(packetSerializer);
    return serializer;
  }
}
