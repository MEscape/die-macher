package com.die_macher.pick_and_place.event;

import com.die_macher.pick_and_place.event.api.ImageRequestedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class ImageRequestedEventTest {
    private Object testSource;

    @BeforeEach
    void setUp() {
        testSource = new Object();
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Add some content to the image to make it more realistic
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 50, 50);
        g2d.setColor(Color.BLUE);
        g2d.fillRect(50, 50, 50, 50);
        g2d.dispose();
    }

    @Test
    @DisplayName("Should create ImageRequestedEvent with valid parameters")
    void shouldCreateImageRequestedEventWithValidParameters() {
        int cubeId = 42;

        ImageRequestedEvent event = new ImageRequestedEvent(testSource, cubeId);

        assertNotNull(event);
        assertSame(testSource, event.getSource());
        assertEquals(cubeId, event.getCubeId());
    }

    @Test
    @DisplayName("Should create ImageRequestedEvent with different cube IDs")
    void shouldCreateImageRequestedEventWithDifferentCubeIds() {
        int cubeId1 = 1;
        int cubeId2 = 999;
        int cubeId3 = -10;

        ImageRequestedEvent event1 = new ImageRequestedEvent(testSource, cubeId1);
        ImageRequestedEvent event2 = new ImageRequestedEvent(testSource, cubeId2);
        ImageRequestedEvent event3 = new ImageRequestedEvent(testSource, cubeId3);

        assertEquals(cubeId1, event1.getCubeId());
        assertEquals(cubeId2, event2.getCubeId());
        assertEquals(cubeId3, event3.getCubeId());
    }

    @Test
    @DisplayName("Should extend ApplicationEvent")
    void shouldExtendApplicationEvent() {
        ImageRequestedEvent event = new ImageRequestedEvent(testSource, 1);

        assertInstanceOf(ApplicationEvent.class, event);
    }

    @Test
    @DisplayName("Should have proper timestamp from ApplicationEvent")
    void shouldHaveProperTimestampFromApplicationEvent() {
        long beforeCreation = System.currentTimeMillis();
        ImageRequestedEvent event = new ImageRequestedEvent(testSource, 1);
        long afterCreation = System.currentTimeMillis();

        assertTrue(event.getTimestamp() >= beforeCreation);
        assertTrue(event.getTimestamp() <= afterCreation);
    }

    @Test
    @DisplayName("Should create events with different sources")
    void shouldCreateEventsWithDifferentSources() {
        Object source1 = new Object();
        Object source2 = "StringSource";
        Object source3 = 456;

        ImageRequestedEvent event1 = new ImageRequestedEvent(source1, 1);
        ImageRequestedEvent event2 = new ImageRequestedEvent(source2, 2);
        ImageRequestedEvent event3 = new ImageRequestedEvent(source3, 3);

        assertSame(source1, event1.getSource());
        assertSame(source2, event2.getSource());
        assertSame(source3, event3.getSource());
    }

    @Test
    @DisplayName("Should handle zero cube ID")
    void shouldHandleZeroCubeId() {
        ImageRequestedEvent event = new ImageRequestedEvent(testSource, 0);

        assertEquals(0, event.getCubeId());
    }

    @Test
    @DisplayName("Should handle maximum integer cube ID")
    void shouldHandleMaximumIntegerCubeId() {
        ImageRequestedEvent event = new ImageRequestedEvent(testSource, Integer.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, event.getCubeId());
    }

    @Test
    @DisplayName("Should handle minimum integer cube ID")
    void shouldHandleMinimumIntegerCubeId() {
        ImageRequestedEvent event = new ImageRequestedEvent(testSource, Integer.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, event.getCubeId());
    }
}
