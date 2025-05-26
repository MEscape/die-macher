package com.die_macher.pick_and_place.service;

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

        Color result = colorDetectionService.detectDominantColor(image);

        assertEquals(Color.RED, result);
    }

    @Test
    @DisplayName("Should detect GREEN as dominant color")
    void detectDominantColor_shouldReturnGreen() {
        // Create a test image with predominantly green pixels
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(30, 255, 30));

        Color result = colorDetectionService.detectDominantColor(image);

        assertEquals(Color.GREEN, result);
    }

    @Test
    @DisplayName("Should detect BLUE as dominant color")
    void detectDominantColor_shouldReturnBlue() {
        // Create a test image with predominantly blue pixels
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(30, 30, 255));

        Color result = colorDetectionService.detectDominantColor(image);

        assertEquals(Color.BLUE, result);
    }

    @Test
    @DisplayName("Should detect YELLOW as dominant color")
    void detectDominantColor_shouldReturnYellow() {
        // Create a test image with predominantly yellow pixels (high red and green, low blue)
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(255, 255, 30));

        Color result = colorDetectionService.detectDominantColor(image);

        assertEquals(Color.YELLOW, result);
    }

    @Test
    @DisplayName("Should not detect YELLOW if red-green difference is too high")
    void detectDominantColor_shouldNotReturnYellow() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(200, 100, 50)); // Difference is > 80

        Color result = colorDetectionService.detectDominantColor(image);

        assertNotEquals(Color.YELLOW, result);
    }

    @Test
    @DisplayName("Should handle borderline yellow")
    void detectDominantColor_shouldReturnYellowOnBorderline() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(151, 151, 90)); // Difference is < 80, should be YELLOW

        Color result = colorDetectionService.detectDominantColor(image);

        assertEquals(Color.YELLOW, result);
    }

    @Test
    @DisplayName("Should handle image with equal color values")
    void detectDominantColor_shouldHandleEqualColors() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        fillImage(image, new Color(128, 128, 128));

        Color result = colorDetectionService.detectDominantColor(image);

        // When colors are equal, it should default to BLUE per implementation
        assertEquals(Color.BLUE, result);
    }

    private void fillImage(BufferedImage image, Color color) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
    }
}
