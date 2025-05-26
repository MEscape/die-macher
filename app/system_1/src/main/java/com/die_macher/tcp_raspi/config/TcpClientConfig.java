package com.die_macher.tcp_raspi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.AbstractClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.messaging.MessageChannel;

@Configuration
class TcpClientConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpClientConfig.class);

    private final TcpProperties tcpProperties;

    public TcpClientConfig(final TcpProperties tcpPropertiesLocal) {
        this.tcpProperties = tcpPropertiesLocal;
    }

    @Bean
    public AbstractClientConnectionFactory clientConnectionFactory() {
        TcpNetClientConnectionFactory factory = new TcpNetClientConnectionFactory(
                tcpProperties.getHost(),
                tcpProperties.getPort()
        );

        factory.setSingleUse(false);

        final ByteArrayLengthHeaderSerializer serializer = new ByteArrayLengthHeaderSerializer(
                tcpProperties.getHeaderSize()
        );
        serializer.setMaxMessageSize(tcpProperties.getMaxMessageSize());

        factory.setDeserializer(serializer);
        factory.setSerializer(serializer);

        LOGGER.info("Creating connection factory to {}:{}", tcpProperties.getHost(), tcpProperties.getPort());

        return factory;
    }

    @Bean
    public TcpReceivingChannelAdapter inboundAdapter(AbstractClientConnectionFactory connectionFactory) {
        TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
        adapter.setConnectionFactory(connectionFactory);
        adapter.setClientMode(true);
        adapter.setOutputChannel(tcpChannel());
        return adapter;
    }

    @Bean
    public MessageChannel tcpChannel() {
        return new DirectChannel();
    }
    
    @Bean
    public TcpSendingMessageHandler tcpSendingMessageHandler(AbstractClientConnectionFactory connectionFactory) {
        TcpSendingMessageHandler messageHandler = new TcpSendingMessageHandler();
        messageHandler.setConnectionFactory(connectionFactory);
        return messageHandler;
    }
}