package com.die_macher.tcp_raspi.infrastructure;

import com.die_macher.pick_and_place.event.api.ImageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundEndpointTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InboundEndpoint inboundEndpoint;

    @BeforeEach
    void setUp() {
        inboundEndpoint = new InboundEndpoint(eventPublisher);
    }

    @Test
    void onMessage_shouldProcessImageAndDetectColor() throws IOException {
        // Prepare test image
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // Prepare message headers
        Map<String, Object> headers = new HashMap<>();
        headers.put(IpHeaders.CONNECTION_ID, "test-connection-1");

        // Create test message
        Message<byte[]> message = new GenericMessage<>(imageBytes, headers);
        
        // Process the message
        inboundEndpoint.processImageMessage(message);
        
        // Verify event was published
        ArgumentCaptor<ImageReceivedEvent> eventCaptor = ArgumentCaptor.forClass(ImageReceivedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        
        // Verify event properties
        ImageReceivedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertNotNull(capturedEvent.getImage());
        assertEquals(100, capturedEvent.getImage().getWidth());
        assertEquals(100, capturedEvent.getImage().getHeight());
        assertEquals(1, capturedEvent.getEventId()); // First cube ID should be 1
    }

    @Test
    void onMessage_shouldHandleInvalidImageData() {
        // Prepare invalid image data
        byte[] invalidImageData = "not an image".getBytes();

        // Prepare message headers
        Map<String, Object> headers = new HashMap<>();
        headers.put(IpHeaders.CONNECTION_ID, "test-connection-2");

        // Create test message
        Message<byte[]> message = new GenericMessage<>(invalidImageData, headers);
        
        // Process the message
        inboundEndpoint.processImageMessage(message);
        
        // Verify no event was published
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void onMessage_shouldHandleNullConnectionId() throws IOException {
        // Prepare test image
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // Create message without connection ID
        Message<byte[]> message = new GenericMessage<>(imageBytes);
        
        // Process the message
        inboundEndpoint.processImageMessage(message);
        
        // Verify event was still published despite missing connection ID
        verify(eventPublisher, times(1)).publishEvent(any(ImageReceivedEvent.class));
    }
}