package com.die_macher.pick_and_place.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PositionTest {
  @Test
  @DisplayName("Should create Position with valid parameters")
  void shouldCreatePositionWithValidParameters() {
    float x = 10.5f;
    float y = 20.3f;
    float z = 5.7f;
    float r = 45.0f;

    Position position = new Position(x, y, z, r);

    assertNotNull(position);
    assertEquals(x, position.x());
    assertEquals(y, position.y());
    assertEquals(z, position.z());
    assertEquals(r, position.r());
  }

  @Test
  @DisplayName("Should create Position with zero values")
  void shouldCreatePositionWithZeroValues() {
    Position position = new Position(0.0f, 0.0f, 0.0f, 0.0f);

    assertEquals(0.0f, position.x());
    assertEquals(0.0f, position.y());
    assertEquals(0.0f, position.z());
    assertEquals(0.0f, position.r());
  }

  @Test
  @DisplayName("Should create Position with negative values")
  void shouldCreatePositionWithNegativeValues() {
    Position position = new Position(-5.5f, -10.2f, -3.8f, -90.0f);

    assertEquals(-5.5f, position.x());
    assertEquals(-10.2f, position.y());
    assertEquals(-3.8f, position.z());
    assertEquals(-90.0f, position.r());
  }

  @Test
  @DisplayName("Should create Position with large values")
  void shouldCreatePositionWithLargeValues() {
    Position position = new Position(1000.0f, 2000.0f, 500.0f, 360.0f);

    assertEquals(1000.0f, position.x());
    assertEquals(2000.0f, position.y());
    assertEquals(500.0f, position.z());
    assertEquals(360.0f, position.r());
  }

  @Test
  @DisplayName("Should handle maximum float values")
  void shouldHandleMaximumFloatValues() {
    Position position =
        new Position(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

    assertEquals(Float.MAX_VALUE, position.x());
    assertEquals(Float.MAX_VALUE, position.y());
    assertEquals(Float.MAX_VALUE, position.z());
    assertEquals(Float.MAX_VALUE, position.r());
  }

  @Test
  @DisplayName("Should handle minimum float values")
  void shouldHandleMinimumFloatValues() {
    Position position =
        new Position(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

    assertEquals(-Float.MAX_VALUE, position.x());
    assertEquals(-Float.MAX_VALUE, position.y());
    assertEquals(-Float.MAX_VALUE, position.z());
    assertEquals(-Float.MAX_VALUE, position.r());
  }

  @Test
  @DisplayName("Should handle very small float values")
  void shouldHandleVerySmallFloatValues() {
    Position position =
        new Position(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    assertEquals(Float.MIN_VALUE, position.x());
    assertEquals(Float.MIN_VALUE, position.y());
    assertEquals(Float.MIN_VALUE, position.z());
    assertEquals(Float.MIN_VALUE, position.r());
  }

  @Test
  @DisplayName("Should handle NaN values")
  void shouldHandleNaNValues() {
    Position position = new Position(Float.NaN, Float.NaN, Float.NaN, Float.NaN);

    assertTrue(Float.isNaN(position.x()));
    assertTrue(Float.isNaN(position.y()));
    assertTrue(Float.isNaN(position.z()));
    assertTrue(Float.isNaN(position.r()));
  }

  @Test
  @DisplayName("Should handle infinity values")
  void shouldHandleInfinityValues() {
    Position positiveInf =
        new Position(
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY);
    Position negativeInf =
        new Position(
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY);

    assertTrue(Float.isInfinite(positiveInf.x()));
    assertTrue(Float.isInfinite(positiveInf.y()));
    assertTrue(Float.isInfinite(positiveInf.z()));
    assertTrue(Float.isInfinite(positiveInf.r()));

    assertTrue(Float.isInfinite(negativeInf.x()));
    assertTrue(Float.isInfinite(negativeInf.y()));
    assertTrue(Float.isInfinite(negativeInf.z()));
    assertTrue(Float.isInfinite(negativeInf.r()));
  }

  @Test
  @DisplayName("Should have proper equality")
  void shouldHaveProperEquality() {
    Position pos1 = new Position(10.0f, 20.0f, 5.0f, 45.0f);
    Position pos2 = new Position(10.0f, 20.0f, 5.0f, 45.0f);
    Position pos3 = new Position(15.0f, 20.0f, 5.0f, 45.0f);
    Position pos4 = new Position(10.0f, 25.0f, 5.0f, 45.0f);
    Position pos5 = new Position(10.0f, 20.0f, 10.0f, 45.0f);
    Position pos6 = new Position(10.0f, 20.0f, 5.0f, 90.0f);

    assertEquals(pos1, pos2);
    assertNotEquals(pos1, pos3);
    assertNotEquals(pos1, pos4);
    assertNotEquals(pos1, pos5);
    assertNotEquals(pos1, pos6);
  }

  @Test
  @DisplayName("Should have proper hashCode")
  void shouldHaveProperHashCode() {
    Position pos1 = new Position(10.0f, 20.0f, 5.0f, 45.0f);
    Position pos2 = new Position(10.0f, 20.0f, 5.0f, 45.0f);
    Position pos3 = new Position(15.0f, 20.0f, 5.0f, 45.0f);

    assertEquals(pos1.hashCode(), pos2.hashCode());
    assertNotEquals(pos1.hashCode(), pos3.hashCode());
  }

  @Test
  @DisplayName("Should have proper toString")
  void shouldHaveProperToString() {
    Position position = new Position(10.5f, 20.3f, 5.7f, 45.0f);
    String toString = position.toString();

    assertNotNull(toString);
    assertTrue(toString.contains("Position"));
    assertTrue(toString.contains("x"));
    assertTrue(toString.contains("y"));
    assertTrue(toString.contains("z"));
    assertTrue(toString.contains("r"));
  }

  @Test
  @DisplayName("Should handle precise decimal values")
  void shouldHandlePreciseDecimalValues() {
    Position position = new Position(3.14159f, 2.71828f, 1.41421f, 1.73205f);

    assertEquals(3.14159f, position.x(), 0.00001f);
    assertEquals(2.71828f, position.y(), 0.00001f);
    assertEquals(1.41421f, position.z(), 0.00001f);
    assertEquals(1.73205f, position.r(), 0.00001f);
  }

  @Test
  @DisplayName("Should create Position with different coordinate combinations")
  void shouldCreatePositionWithDifferentCoordinateCombinations() {
    Position origin = new Position(0f, 0f, 0f, 0f);
    Position unitX = new Position(1f, 0f, 0f, 0f);
    Position unitY = new Position(0f, 1f, 0f, 0f);
    Position unitZ = new Position(0f, 0f, 1f, 0f);
    Position unitR = new Position(0f, 0f, 0f, 1f);

    assertEquals(0f, origin.x());
    assertEquals(1f, unitX.x());
    assertEquals(1f, unitY.y());
    assertEquals(1f, unitZ.z());
    assertEquals(1f, unitR.r());
  }
}
