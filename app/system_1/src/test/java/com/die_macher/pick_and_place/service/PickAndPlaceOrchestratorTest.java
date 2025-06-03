package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.event.api.ImageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickAndPlaceOrchestratorTest {

    @Mock
    private RobotMovementService robotMovementService;

    @Mock
    private StackTracker stackTracker;

    @Mock
    private ColorDetectionService colorDetectionService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PickAndPlaceOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new PickAndPlaceOrchestrator(
                robotMovementService,
                stackTracker,
                colorDetectionService,
                eventPublisher
        );
    }

    @Test
    void handleColorDetected_shouldProcessImageAndDetectColor() {
        // Arrange
        int cubeId = 42;
        BufferedImage mockImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Color expectedColor = Color.BLUE;
        
        // Mock color detection
        when(colorDetectionService.detectDominantColor(mockImage)).thenReturn(expectedColor);
        
        // Create received event
        ImageReceivedEvent receivedEvent = new ImageReceivedEvent(this, mockImage, cubeId);
        
        // Act
        orchestrator.handleColorDetected(receivedEvent);
        
        // Assert
        verify(colorDetectionService).detectDominantColor(mockImage);
    }

    @Test
    void startPickAndPlace_shouldHandleExceptions() {
        // Arrange
        doThrow(new RuntimeException("Robot error")).when(robotMovementService).initialize();
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orchestrator.startPickAndPlace(1);
        });
        
        assertEquals("Pick and place operation failed", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Robot error"));
    }

    @Test
    void processCube_shouldHandleExceptions() {
        // Arrange
        doThrow(new RuntimeException("Movement error")).when(robotMovementService).pickupCube(anyInt());
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            orchestrator.startPickAndPlace(1);
        });
        
        assertEquals("Pick and place operation failed", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Failed to process cube"));
    }
}
