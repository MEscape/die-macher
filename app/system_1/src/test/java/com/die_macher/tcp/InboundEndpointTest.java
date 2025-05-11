package com.die_macher.tcp;

import com.die_macher.service.ColorDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundEndpointTest {

    @Mock
    private ColorDetectionService colorDetectionService;

    private InboundEndpoint inboundEndpoint;

    @BeforeEach
    void setUp() {
        inboundEndpoint = new InboundEndpoint(colorDetectionService);
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

        // Configure mock
        when(colorDetectionService.detectDominantColor(any(BufferedImage.class)))
                .thenReturn("RED");

        // Execute
        inboundEndpoint.onMessage(message);

        // Verify
        verify(colorDetectionService).detectDominantColor(any(BufferedImage.class));
    }

    @Test
    void onMessage_shouldHandleInvalidImageData() throws IOException {
        // Prepare invalid image data
        byte[] invalidImageData = "not an image".getBytes();

        // Prepare message headers
        Map<String, Object> headers = new HashMap<>();
        headers.put(IpHeaders.CONNECTION_ID, "test-connection-2");

        // Create test message
        Message<byte[]> message = new GenericMessage<>(invalidImageData, headers);

        // Execute and verify no exception is thrown
        inboundEndpoint.onMessage(message);

        // Verify color detection was not called
        verify(colorDetectionService, never()).detectDominantColor(any(BufferedImage.class));
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

        // Configure mock
        when(colorDetectionService.detectDominantColor(any(BufferedImage.class)))
                .thenReturn("BLUE");

        // Execute
        inboundEndpoint.onMessage(message);

        // Verify color detection still works
        verify(colorDetectionService).detectDominantColor(any(BufferedImage.class));
    }
}