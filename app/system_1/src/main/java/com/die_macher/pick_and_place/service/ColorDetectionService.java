package com.die_macher.pick_and_place.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;

@Service
public class ColorDetectionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColorDetectionService.class);
    private static final int MIN_BRIGHTNESS_THRESHOLD = 30;
    private static final double YELLOW_RED_GREEN_MIN_PERCENT = 0.25;
    private static final double YELLOW_BLUE_MAX_PERCENT = 0.30; // Tightened from 0.35
    private static final double YELLOW_BALANCE_TOLERANCE = 0.20; // Tightened from 0.25
    private static final double YELLOW_DOMINANCE_THRESHOLD = 0.70; // Increased from 0.65

    public Color detectDominantColor(BufferedImage image) {
        LOGGER.debug("Analyzing image of size {}x{}", image.getWidth(), image.getHeight());

        ColorStats stats = calculateColorStats(image);
        Color detectedColor = determineColor(stats);

        LOGGER.info("Detected color: {} from stats R:{}, G:{}, B:{}",
                detectedColor, stats.red(), stats.green(), stats.blue());

        return detectedColor;
    }

    private ColorStats calculateColorStats(BufferedImage image) {
        long redSum = 0, greenSum = 0, blueSum = 0;
        int totalPixels = image.getWidth() * image.getHeight();

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                redSum += (rgb >> 16) & 0xFF;
                greenSum += (rgb >> 8) & 0xFF;
                blueSum += rgb & 0xFF;
            }
        }

        return new ColorStats(
                redSum / totalPixels,
                greenSum / totalPixels,
                blueSum / totalPixels
        );
    }

    private boolean isYellow(ColorStats stats) {
        double totalIntensity = stats.red() + stats.green() + stats.blue();

        if (totalIntensity < MIN_BRIGHTNESS_THRESHOLD) {
            return false;
        }

        double redPercent = stats.red() / totalIntensity;
        double greenPercent = stats.green() / totalIntensity;
        double bluePercent = stats.blue() / totalIntensity;

        boolean strongRedGreen = (redPercent > YELLOW_RED_GREEN_MIN_PERCENT &&
                greenPercent > YELLOW_RED_GREEN_MIN_PERCENT);
        boolean weakBlue = (bluePercent < YELLOW_BLUE_MAX_PERCENT);
        boolean balancedRedGreen = Math.abs(redPercent - greenPercent) < YELLOW_BALANCE_TOLERANCE;
        boolean redGreenDominant = (redPercent + greenPercent) > YELLOW_DOMINANCE_THRESHOLD;

        return strongRedGreen && weakBlue && balancedRedGreen && redGreenDominant;
    }

    private Color determineColor(ColorStats stats) {
        if (isYellow(stats)) {
            return Color.YELLOW;
        } else if (stats.red() > stats.green() && stats.red() > stats.blue()) {
            return Color.RED;
        } else if (stats.green() > stats.red() && stats.green() > stats.blue()) {
            return Color.GREEN;
        } else {
            return Color.BLUE;
        }
    }

    private record ColorStats(long red, long green, long blue) {}
}