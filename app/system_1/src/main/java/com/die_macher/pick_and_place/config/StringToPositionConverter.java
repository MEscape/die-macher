package com.die_macher.pick_and_place.config;

import com.die_macher.pick_and_place.model.Position;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
public class StringToPositionConverter implements Converter<String, Position> {

  @Override
  public Position convert(String source) {
    String[] parts = source.split(",");
    if (parts.length != 4) {
      throw new IllegalArgumentException("Position must have 4 comma-separated values");
    }

    try {
      float x = Float.parseFloat(parts[0].trim());
      float y = Float.parseFloat(parts[1].trim());
      float z = Float.parseFloat(parts[2].trim());
      float r = Float.parseFloat(parts[3].trim());
      return new Position(x, y, z, r);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid number format in Position: " + source, e);
    }
  }
}
