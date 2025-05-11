package com.die_macher.service;

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

    private String determineColor(ColorStats stats) {
        LOGGER.debug("Color stats - R: {}, G: {}, B: {}", stats.red(), stats.green(), stats.blue());

        if (stats.red > stats.green && stats.red > stats.blue) {
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