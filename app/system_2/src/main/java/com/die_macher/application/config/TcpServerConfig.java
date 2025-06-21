package com.die_macher.application.config;

import com.die_macher.infrastructure.adapter.serializer.CustomHeaderDeserializer;
import com.die_macher.infrastructure.config.properties.TcpServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ErrorMessage;

@Configuration
public class TcpServerConfig {

    public static final String ERROR_CHANNEL = "errorChannel";

    private static final Logger LOGGER = LoggerFactory.getLogger(TcpServerConfig.class);
    private final TcpServerProperties tcpServerProperties;

    public TcpServerConfig(TcpServerProperties tcpServerProperties) {
        this.tcpServerProperties = tcpServerProperties;
    }

    @Bean
    public AbstractServerConnectionFactory tcpServerConnectionFactory(CustomHeaderDeserializer deserializer) {
        TcpNioServerConnectionFactory factory = new TcpNioServerConnectionFactory(tcpServerProperties.getPort());

        factory.setDeserializer(deserializer);

        // Connection settings
        factory.setBacklog(tcpServerProperties.getBacklog());
        factory.setSoTimeout(tcpServerProperties.getSoTimeout());
        factory.setSingleUse(false); // Reuse connections

        LOGGER.info("TCP Server configured on port {} with max message size {}",
                tcpServerProperties.getPort(), tcpServerProperties.getMaxMessageSize());

        return factory;
    }

    @Bean
    public TcpInboundGateway tcpInboundGateway(AbstractServerConnectionFactory connectionFactory) {
        TcpInboundGateway gateway = new TcpInboundGateway();
        gateway.setConnectionFactory(connectionFactory);
        gateway.setRequestChannel(tcpInputChannel());
        gateway.setErrorChannel(errorChannel());
        return gateway;
    }

    @Bean
    public CustomHeaderDeserializer customHeaderDeserializer() {
        return new CustomHeaderDeserializer(
                tcpServerProperties.getHeaderSize(),
                tcpServerProperties.getMaxMessageSize()
        );
    }

    @Bean
    public MessageChannel tcpInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel errorChannel() {
        return new DirectChannel();
    }

    @ServiceActivator(inputChannel = ERROR_CHANNEL)
    public void handleError(ErrorMessage errorMessage) {
        Throwable cause = errorMessage.getPayload();
        LOGGER.error("Processing error occurred", cause);
    }
}
