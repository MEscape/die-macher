package com.die_macher.tcp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.messaging.Message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboundEndpointTest {

    @Mock
    private TcpSendingMessageHandler tcpSendingMessageHandler;

    private OutboundEndpoint outboundEndpoint;

    @BeforeEach
    void setUp() {
        outboundEndpoint = new OutboundEndpoint(tcpSendingMessageHandler);
    }

    @Test
    void requestImage_shouldSendMessageToServer() {
        outboundEndpoint.requestImage();

        verify(tcpSendingMessageHandler, times(1)).handleMessage(any(Message.class));
    }
}