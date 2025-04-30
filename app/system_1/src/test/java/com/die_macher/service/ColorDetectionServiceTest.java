package com.die_macher.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.awt.image.BufferedImage;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class ColorDetectionServiceTest {

    private ColorDetectionService colorDetectionService;

    @BeforeEach
    void setUp() {
        colorDetectionService = new ColorDetectionService();
    }

    @Test
    @DisplayName("Should detect RED as dominant color")
    void detectDominantColor_shouldReturnRed() {
        // Create a test image with predominantly red pixels
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(255, 30, 30));

        String result = colorDetectionService.detectDominantColor(image);

        assertEquals("RED", result);
    }

    @Test
    @DisplayName("Should detect GREEN as dominant color")
    void detectDominantColor_shouldReturnGreen() {
        // Create a test image with predominantly green pixels
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(30, 255, 30));

        String result = colorDetectionService.detectDominantColor(image);

        assertEquals("GREEN", result);
    }

    @Test
    @DisplayName("Should detect BLUE as dominant color")
    void detectDominantColor_shouldReturnBlue() {
        // Create a test image with predominantly blue pixels
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(30, 30, 255));

        String result = colorDetectionService.detectDominantColor(image);

        assertEquals("BLUE", result);
    }

    @Test
    @DisplayName("Should handle image with equal color values")
    void detectDominantColor_shouldHandleEqualColors() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(128, 128, 128));

        String result = colorDetectionService.detectDominantColor(image);

        // When colors are equal, it should default to BLUE per implementation
        assertEquals("BLUE", result);
    }

    private void fillImage(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
    }
}
