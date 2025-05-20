package com.die_macher.pick_and_place.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;

@Service
public class ColorDetectionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorDetectionService.class);

    public String detectDominantColor(BufferedImage image) {
        LOGGER.debug("Analyzing image of size {}x{}", image.getWidth(), image.getHeight());
        ColorStats colorStats = calculateColorStats(image);
        return determineColor(colorStats);
    }

    private ColorStats calculateColorStats(BufferedImage image) {
        long redSum = 0, greenSum = 0, blueSum = 0;
        int total = image.getWidth() * image.getHeight();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                redSum += (rgb >> 16) & 0xFF;
                greenSum += (rgb >> 8) & 0xFF;
                blueSum += rgb & 0xFF;
            }
        }

        return new ColorStats(
                redSum / total,
                greenSum / total,
                blueSum / total
        );
    }

    private boolean isYellow(ColorStats stats) {
        // Total color intensity
        double totalIntensity = stats.red + stats.green + stats.blue;

        // Skip processing if the color is too dark overall
        if (totalIntensity < 150) {
            return false;
        }

        // Calculate each color's contribution percentage
        double redPercent = stats.red / totalIntensity;
        double greenPercent = stats.green / totalIntensity;
        double bluePercent = stats.blue / totalIntensity;

        // Yellow has high red and green percentages, and low blue percentage
        boolean strongRedGreen = (redPercent > 0.3 && greenPercent > 0.3);
        boolean weakBlue = (bluePercent < 0.25);

        // For yellow, red and green should be relatively balanced
        boolean balancedRedGreen = Math.abs(redPercent - greenPercent) < 0.15;

        // Additional check: combined red+green should significantly outweigh blue
        boolean redGreenDominant = (redPercent + greenPercent) > 0.75;

        return strongRedGreen && weakBlue && balancedRedGreen && redGreenDominant;
    }

    private String determineColor(ColorStats stats) {
        LOGGER.debug("Color stats - R: {}, G: {}, B: {}", stats.red(), stats.green(), stats.blue());

        if (isYellow(stats)) {
            return "YELLOW";
        } else if (stats.red > stats.green && stats.red > stats.blue) {
            return "RED";
        } else if (stats.green > stats.red && stats.green > stats.blue) {
            return "GREEN";
        } else {
            return "BLUE";
        }
    }

    private record ColorStats(long red, long green, long blue) {
    }
}