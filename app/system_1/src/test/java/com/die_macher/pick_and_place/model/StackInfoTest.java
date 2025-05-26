package com.die_macher.pick_and_place.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class StackInfoTest {
    @Test
    @DisplayName("Should create StackInfo with valid parameters")
    void shouldCreateStackInfoWithValidParameters() {
        Color color = Color.RED;
        int height = 5;

        StackInfo stackInfo = new StackInfo(color, height);

        assertNotNull(stackInfo);
        assertEquals(color, stackInfo.color());
        assertEquals(height, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should create StackInfo with different colors")
    void shouldCreateStackInfoWithDifferentColors() {
        StackInfo redStack = new StackInfo(Color.RED, 3);
        StackInfo blueStack = new StackInfo(Color.BLUE, 7);
        StackInfo greenStack = new StackInfo(Color.GREEN, 2);
        StackInfo yellowStack = new StackInfo(Color.YELLOW, 1);

        assertEquals(Color.RED, redStack.color());
        assertEquals(Color.BLUE, blueStack.color());
        assertEquals(Color.GREEN, greenStack.color());
        assertEquals(Color.YELLOW, yellowStack.color());
        assertEquals(3, redStack.currentHeight());
        assertEquals(7, blueStack.currentHeight());
        assertEquals(2, greenStack.currentHeight());
        assertEquals(1, yellowStack.currentHeight());
    }

    @Test
    @DisplayName("Should create StackInfo with custom Color")
    void shouldCreateStackInfoWithCustomColor() {
        Color customColor = new Color(128, 64, 192);
        int height = 10;

        StackInfo stackInfo = new StackInfo(customColor, height);

        assertEquals(customColor, stackInfo.color());
        assertEquals(128, stackInfo.color().getRed());
        assertEquals(64, stackInfo.color().getGreen());
        assertEquals(192, stackInfo.color().getBlue());
        assertEquals(height, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should handle null color")
    void shouldHandleNullColor() {
        StackInfo stackInfo = new StackInfo(null, 5);

        assertNull(stackInfo.color());
        assertEquals(5, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should handle zero height")
    void shouldHandleZeroHeight() {
        StackInfo stackInfo = new StackInfo(Color.BLUE, 0);

        assertEquals(Color.BLUE, stackInfo.color());
        assertEquals(0, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should handle negative height")
    void shouldHandleNegativeHeight() {
        StackInfo stackInfo = new StackInfo(Color.GREEN, -3);

        assertEquals(Color.GREEN, stackInfo.color());
        assertEquals(-3, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should handle maximum integer height")
    void shouldHandleMaximumIntegerHeight() {
        StackInfo stackInfo = new StackInfo(Color.ORANGE, Integer.MAX_VALUE);

        assertEquals(Color.ORANGE, stackInfo.color());
        assertEquals(Integer.MAX_VALUE, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should handle minimum integer height")
    void shouldHandleMinimumIntegerHeight() {
        StackInfo stackInfo = new StackInfo(Color.PINK, Integer.MIN_VALUE);

        assertEquals(Color.PINK, stackInfo.color());
        assertEquals(Integer.MIN_VALUE, stackInfo.currentHeight());
    }

    @Test
    @DisplayName("Should have proper equality")
    void shouldHaveProperEquality() {
        StackInfo stack1 = new StackInfo(Color.RED, 5);
        StackInfo stack2 = new StackInfo(Color.RED, 5);
        StackInfo stack3 = new StackInfo(Color.BLUE, 5);
        StackInfo stack4 = new StackInfo(Color.RED, 3);

        assertEquals(stack1, stack2);
        assertNotEquals(stack1, stack3);
        assertNotEquals(stack1, stack4);
        assertNotEquals(stack3, stack4);
    }

    @Test
    @DisplayName("Should have proper hashCode")
    void shouldHaveProperHashCode() {
        StackInfo stack1 = new StackInfo(Color.RED, 5);
        StackInfo stack2 = new StackInfo(Color.RED, 5);
        StackInfo stack3 = new StackInfo(Color.BLUE, 5);

        assertEquals(stack1.hashCode(), stack2.hashCode());
        assertNotEquals(stack1.hashCode(), stack3.hashCode());
    }

    @Test
    @DisplayName("Should have proper toString")
    void shouldHaveProperToString() {
        StackInfo stackInfo = new StackInfo(Color.RED, 5);
        String toString = stackInfo.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("StackInfo"));
        assertTrue(toString.contains("color"));
        assertTrue(toString.contains("currentHeight"));
    }

    @Test
    @DisplayName("Should handle Color with alpha channel")
    void shouldHandleColorWithAlphaChannel() {
        Color colorWithAlpha = new Color(255, 128, 64, 200);
        StackInfo stackInfo = new StackInfo(colorWithAlpha, 8);

        assertEquals(colorWithAlpha, stackInfo.color());
        assertEquals(255, stackInfo.color().getRed());
        assertEquals(128, stackInfo.color().getGreen());
        assertEquals(64, stackInfo.color().getBlue());
        assertEquals(200, stackInfo.color().getAlpha());
        assertEquals(8, stackInfo.currentHeight());
    }
}
