package com.die_macher.pick_and_place.service;

import static org.junit.jupiter.api.Assertions.*;

import com.die_macher.pick_and_place.model.StackInfo;
import java.awt.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StackTracker Tests")
class StackTrackerTest {

  private StackTracker stackTracker;

  @BeforeEach
  void setUp() {
    stackTracker = new StackTracker();
  }

  @Test
  @DisplayName("Should initialize stacks to zero and add cube correctly")
  void shouldAddCubeAndReturnCorrectStackInfo() {
    // When
    StackInfo result = stackTracker.addCube(Color.RED);

    // Then
    assertEquals(Color.RED, result.color());
    assertEquals(1, result.currentHeight());
  }

  @Test
  @DisplayName("Should increment stack height for same color")
  void shouldIncrementStackHeightForSameColor() {
    // When
    StackInfo first = stackTracker.addCube(Color.RED);
    StackInfo second = stackTracker.addCube(Color.RED);

    // Then
    assertEquals(1, first.currentHeight());
    assertEquals(2, second.currentHeight());
  }

  @Test
  @DisplayName("Should handle different colors independently")
  void shouldHandleDifferentColorsIndependently() {
    // When
    StackInfo red = stackTracker.addCube(Color.RED);
    StackInfo blue = stackTracker.addCube(Color.BLUE);

    // Then
    assertEquals(1, red.currentHeight());
    assertEquals(1, blue.currentHeight());
  }

  @Test
  @DisplayName("Should reset all stacks to zero")
  void shouldResetAllStacksToZero() {
    // Given
    stackTracker.addCube(Color.RED);
    stackTracker.addCube(Color.RED);

    // When
    stackTracker.reset();
    StackInfo result = stackTracker.addCube(Color.RED);

    // Then
    assertEquals(1, result.currentHeight());
  }
}
