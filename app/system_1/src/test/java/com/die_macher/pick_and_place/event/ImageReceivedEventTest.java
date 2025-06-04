package com.die_macher.pick_and_place.event;

import static org.junit.jupiter.api.Assertions.*;

import com.die_macher.pick_and_place.event.api.ImageReceivedEvent;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;

class ImageReceivedEventTest {
  private Object testSource;
  private BufferedImage testImage;

  @BeforeEach
  void setUp() {
    testSource = new Object();
    testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

    // Add some content to the image to make it more realistic
    Graphics2D g2d = testImage.createGraphics();
    g2d.setColor(Color.RED);
    g2d.fillRect(0, 0, 50, 50);
    g2d.setColor(Color.BLUE);
    g2d.fillRect(50, 50, 50, 50);
    g2d.dispose();
  }

  @Test
  @DisplayName("Should create ImageReceivedEvent with valid parameters")
  void shouldCreateImageReceivedEventWithValidParameters() {
    int cubeId = 42;

    ImageReceivedEvent event = new ImageReceivedEvent(testSource, testImage, cubeId);

    assertNotNull(event);
    assertSame(testSource, event.getSource());
    assertSame(testImage, event.getImage());
    assertEquals(cubeId, event.getEventId());
  }

  @Test
  @DisplayName("Should create ImageReceivedEvent with different cube IDs")
  void shouldCreateImageReceivedEventWithDifferentCubeIds() {
    int cubeId1 = 1;
    int cubeId2 = 999;
    int cubeId3 = -5;

    ImageReceivedEvent event1 = new ImageReceivedEvent(testSource, testImage, cubeId1);
    ImageReceivedEvent event2 = new ImageReceivedEvent(testSource, testImage, cubeId2);
    ImageReceivedEvent event3 = new ImageReceivedEvent(testSource, testImage, cubeId3);

    assertEquals(cubeId1, event1.getEventId());
    assertEquals(cubeId2, event2.getEventId());
    assertEquals(cubeId3, event3.getEventId());
  }

  @Test
  @DisplayName("Should create ImageReceivedEvent with null image")
  void shouldCreateImageReceivedEventWithNullImage() {
    int cubeId = 10;

    ImageReceivedEvent event = new ImageReceivedEvent(testSource, null, cubeId);

    assertNotNull(event);
    assertSame(testSource, event.getSource());
    assertNull(event.getImage());
    assertEquals(cubeId, event.getEventId());
  }

  @Test
  @DisplayName("Should create ImageReceivedEvent with different image types")
  void shouldCreateImageReceivedEventWithDifferentImageTypes() {
    BufferedImage rgbImage = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
    BufferedImage argbImage = new BufferedImage(75, 75, BufferedImage.TYPE_INT_ARGB);
    BufferedImage grayImage = new BufferedImage(25, 25, BufferedImage.TYPE_BYTE_GRAY);
    int cubeId = 5;

    ImageReceivedEvent event1 = new ImageReceivedEvent(testSource, rgbImage, cubeId);
    ImageReceivedEvent event2 = new ImageReceivedEvent(testSource, argbImage, cubeId);
    ImageReceivedEvent event3 = new ImageReceivedEvent(testSource, grayImage, cubeId);

    assertSame(rgbImage, event1.getImage());
    assertSame(argbImage, event2.getImage());
    assertSame(grayImage, event3.getImage());
    assertEquals(BufferedImage.TYPE_INT_RGB, event1.getImage().getType());
    assertEquals(BufferedImage.TYPE_INT_ARGB, event2.getImage().getType());
    assertEquals(BufferedImage.TYPE_BYTE_GRAY, event3.getImage().getType());
  }

  @Test
  @DisplayName("Should extend ApplicationEvent")
  void shouldExtendApplicationEvent() {
    ImageReceivedEvent event = new ImageReceivedEvent(testSource, testImage, 1);

    assertInstanceOf(ApplicationEvent.class, event);
  }

  @Test
  @DisplayName("Should have proper timestamp from ApplicationEvent")
  void shouldHaveProperTimestampFromApplicationEvent() {
    long beforeCreation = System.currentTimeMillis();
    ImageReceivedEvent event = new ImageReceivedEvent(testSource, testImage, 1);
    long afterCreation = System.currentTimeMillis();

    assertTrue(event.getTimestamp() >= beforeCreation);
    assertTrue(event.getTimestamp() <= afterCreation);
  }

  @Test
  @DisplayName("Should create events with different sources")
  void shouldCreateEventsWithDifferentSources() {
    Object source1 = new Object();
    Object source2 = "StringSource";
    Object source3 = 123;

    ImageReceivedEvent event1 = new ImageReceivedEvent(source1, testImage, 1);
    ImageReceivedEvent event2 = new ImageReceivedEvent(source2, testImage, 2);
    ImageReceivedEvent event3 = new ImageReceivedEvent(source3, testImage, 3);

    assertSame(source1, event1.getSource());
    assertSame(source2, event2.getSource());
    assertSame(source3, event3.getSource());
  }

  @Test
  @DisplayName("Should handle zero cube ID")
  void shouldHandleZeroCubeId() {
    ImageReceivedEvent event = new ImageReceivedEvent(testSource, testImage, 0);

    assertEquals(0, event.getEventId());
  }
}
