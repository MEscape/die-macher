package com.die_macher.tcp_server.config;

import com.die_macher.tcp_server.serializer.CustomHeaderSerializer;
import com.die_macher.tcp_server.serializer.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.messaging.MessageChannel;

@Configuration
public class TcpServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServerConfig.class);

    private final TcpServerProperties tcpProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TcpServerConfig(final TcpServerProperties tcpProperties, ApplicationEventPublisher applicationEventPublisher) {
        this.tcpProperties = tcpProperties;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Bean(name = "serverConnectionFactory")
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNetServerConnectionFactory factory = new TcpNetServerConnectionFactory(
                tcpProperties.getPort()
        );

        factory.setLocalAddress(tcpProperties.getHost());
        factory.setSingleUse(false);
        factory.setApplicationEventPublisher(applicationEventPublisher);

        final ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer(4);
        serializer.setMaxMessageSize(tcpProperties.getMaxMessageSize());

        JsonSerializer jsonSerializer = new JsonSerializer(tcpProperties.getMaxMessageSize());

        factory.setSerializer(new CustomHeaderSerializer(5, Integer.MAX_VALUE));
        factory.setDeserializer(jsonSerializer);

        LOGGER.info("Creating server connection factory on {}:{}", tcpProperties.getHost(), tcpProperties.getPort());

        return factory;
    }

    @Bean
    public TcpInboundGateway tcpInboundGateway(@Qualifier("serverConnectionFactory") AbstractServerConnectionFactory connectionFactory) {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(connectionFactory);
        gateway.setRequestChannel(serverChannel());
        return gateway;
    }

    @Bean
    public MessageChannel serverChannel() {
        return new DirectChannel();
    }

}