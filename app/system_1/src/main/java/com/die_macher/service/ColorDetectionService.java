package com.die_macher.service;

import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;

@Service
public class ColorDetectionService {
    public String detectDominantColor(BufferedImage image) {
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