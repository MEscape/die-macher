package com.die_macher.pick_and_place.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.die_macher.pick_and_place.config.RobotConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HeightCalculator Tests")
class HeightCalculatorTest {

  @Mock private RobotConfiguration robotConfiguration;

  @Mock private RobotConfiguration.PhysicalConstants physicalConstants;

  private HeightCalculator heightCalculator;

  // Test constants
  private static final float CUBE_HEIGHT = 10.0f;
  private static final float OFFSET = 5.0f;
  private static final float ABSOLUTE_FLOOR = 2.0f;

  @BeforeEach
  void setUp() {
    // Setup mock behavior
    when(robotConfiguration.physicalConstants()).thenReturn(physicalConstants);
    when(physicalConstants.cubeHeight()).thenReturn(CUBE_HEIGHT);

    // Initialize the service under test
    heightCalculator = new HeightCalculator(robotConfiguration);
  }

  @Test
  @DisplayName("Should calculate approach height correctly for stack position 0")
  void shouldCalculateApproachHeightForPositionZero() {
    when(physicalConstants.offset()).thenReturn(OFFSET);
    when(physicalConstants.absoluteFloor()).thenReturn(ABSOLUTE_FLOOR);

    // Given
    int stackPosition = 0;
    float expectedHeight = 0 * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR; // 0 + 5 + 2 = 7.0

    // When
    float actualHeight = heightCalculator.calculateApproachHeight(stackPosition);
    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate approach height correctly for positive stack position")
  void shouldCalculateApproachHeightForPositivePosition() {
    when(physicalConstants.offset()).thenReturn(OFFSET);
    when(physicalConstants.absoluteFloor()).thenReturn(ABSOLUTE_FLOOR);

    // Given
    int stackPosition = 3;
    float expectedHeight = 3 * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR; // 30 + 5 + 2 = 37.0

    // When
    float actualHeight = heightCalculator.calculateApproachHeight(stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate approach height correctly for negative stack position")
  void shouldCalculateApproachHeightForNegativePosition() {
    when(physicalConstants.offset()).thenReturn(OFFSET);
    when(physicalConstants.absoluteFloor()).thenReturn(ABSOLUTE_FLOOR);

    // Given
    int stackPosition = -2;
    float expectedHeight = -2 * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR; // -20 + 5 + 2 = -13.0

    // When
    float actualHeight = heightCalculator.calculateApproachHeight(stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate pickup height correctly for stack position 0")
  void shouldCalculatePickupHeightForPositionZero() {
    when(physicalConstants.absoluteFloor()).thenReturn(ABSOLUTE_FLOOR);

    // Given
    int stackPosition = 0;
    float expectedHeight = 0 * CUBE_HEIGHT + ABSOLUTE_FLOOR; // 0 + 2 = 2.0

    // When
    float actualHeight = heightCalculator.calculatePickupHeight(stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate pickup height correctly for negative stack position")
  void shouldCalculatePickupHeightForNegativePosition() {
    when(physicalConstants.absoluteFloor()).thenReturn(ABSOLUTE_FLOOR);

    // Given
    int stackPosition = -1;
    float expectedHeight =
        -1 * CUBE_HEIGHT + ABSOLUTE_FLOOR - stackPosition * 0.75F; // -10 + 2 = -8.0

    // When
    float actualHeight = heightCalculator.calculatePickupHeight(stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate lift height correctly for stack position 0")
  void shouldCalculateLiftHeightForPositionZero() {
    // Given
    int stackPosition = 0;
    float prevHeight = 20.0f;
    float rawLift = (stackPosition * CUBE_HEIGHT + CUBE_HEIGHT) - prevHeight; // (0+10)-20 = -10
    float expectedHeight = Math.max(rawLift, 0f); // 0.0

    // When
    float actualHeight = heightCalculator.calculateLiftHeight(prevHeight, stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate lift height correctly for positive stack position")
  void shouldCalculateLiftHeightForPositivePosition() {
    // Given
    int stackPosition = 4;
    float prevHeight = 20.0f;
    float rawLift = (stackPosition * CUBE_HEIGHT + CUBE_HEIGHT) - prevHeight; // (40+10)-20=30
    float expectedHeight = Math.max(rawLift, 0f); // 30.0

    // When
    float actualHeight = heightCalculator.calculateLiftHeight(prevHeight, stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should calculate lift height correctly for negative stack position")
  void shouldCalculateLiftHeightForNegativePosition() {
    // Given
    int stackPosition = -3;
    float prevHeight = 20.0f;
    float rawLift = (stackPosition * CUBE_HEIGHT + CUBE_HEIGHT) - prevHeight; // (-30+10)-20 = -40
    float expectedHeight = Math.max(rawLift, 0f); // 0.0

    // When
    float actualHeight = heightCalculator.calculateLiftHeight(prevHeight, stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should handle edge case with maximum integer stack position for approach height")
  void shouldHandleMaxIntegerStackPositionForApproachHeight() {
    // Given
    int stackPosition = Integer.MAX_VALUE;
    float expectedHeight = (float) Integer.MAX_VALUE * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR;

    // When
    float actualHeight = heightCalculator.calculateApproachHeight(stackPosition);

    // Then
    assertEquals(expectedHeight, actualHeight, 0.001f);
  }

  @Test
  @DisplayName("Should verify constructor initializes constants correctly")
  void shouldInitializeConstantsCorrectly() {
    when(physicalConstants.offset()).thenReturn(OFFSET);
    when(physicalConstants.absoluteFloor()).thenReturn(ABSOLUTE_FLOOR);

    // Then - verify that calculations work (implicitly tests constructor)
    float approachHeight = heightCalculator.calculateApproachHeight(1);
    float expectedApproachHeight = 1 * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR; // 10 + 5 + 2 = 17.0

    assertEquals(expectedApproachHeight, approachHeight, 0.001f);
  }
}
