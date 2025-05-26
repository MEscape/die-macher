package com.die_macher.tcp_raspi.infrastructure;

import com.die_macher.pick_and_place.event.api.ImageRequestedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        // Create test event
        ImageRequestedEvent event = new ImageRequestedEvent(this, 42);
        
        // Call the method
        outboundEndpoint.requestImage(event);
        
        // Capture and verify the message
        ArgumentCaptor<Message<?>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(tcpSendingMessageHandler, times(1)).handleMessage(messageCaptor.capture());
        
        // Verify message content
        Message<?> capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertInstanceOf(byte[].class, capturedMessage.getPayload());
        assertEquals("SEND_IMAGE", new String((byte[]) capturedMessage.getPayload()));
    }

    @Test
    void requestImage_shouldHandleExceptionGracefully() {
        // Create test event
        ImageRequestedEvent event = new ImageRequestedEvent(this, 42);
        
        // Setup mock to throw exception
        doThrow(new MessagingException("Test exception")).when(tcpSendingMessageHandler).handleMessage(any(Message.class));
        
        // Call the method and expect exception
        Exception exception = assertThrows(RuntimeException.class, () -> {
            outboundEndpoint.requestImage(event);
        });
        
        // Verify exception details
        assertEquals("Image request failed", exception.getMessage());
        assertInstanceOf(MessagingException.class, exception.getCause());
        assertEquals("Test exception", exception.getCause().getMessage());
        
        // Verify the message handler was called
        verify(tcpSendingMessageHandler, times(1)).handleMessage(any(Message.class));
    }
}