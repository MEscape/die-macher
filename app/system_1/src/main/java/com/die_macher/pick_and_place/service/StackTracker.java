package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.model.StackInfo;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class StackTracker {
  private static final List<Color> SUPPORTED_COLORS =
      List.of(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);

  private final Map<Color, Integer> colorStacks = new ConcurrentHashMap<>();

  public StackTracker() {
    // Initialize all stacks to 0
    for (Color color : SUPPORTED_COLORS) {
      colorStacks.put(color, 0);
    }
  }

  public StackInfo addCube(Color color) {
    int newHeight = colorStacks.merge(color, 1, Integer::sum);
    return new StackInfo(color, newHeight);
  }

  public StackInfo removeCube(Color color) {
    int newHeight = colorStacks.merge(color, -1, Integer::sum);
    new StackInfo(color, newHeight);
    return new StackInfo(color, newHeight);
  }

  public void reset() {
    colorStacks.replaceAll((k, v) -> 0);
  }

  public int getMaxStackHeight() {
    return colorStacks.values().stream()
        .mapToInt(Integer::intValue)
        .max()
        .orElse(0); // Return 0 if map is empty
  }
}
