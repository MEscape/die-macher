package com.die_macher.pick_and_place.event.api;

import java.awt.image.BufferedImage;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ImageReceivedEvent extends ApplicationEvent {
  private final BufferedImage image;
  private final int eventId;

  public ImageReceivedEvent(Object source, BufferedImage image, int eventId) {
    super(source);
    this.image = image;
    this.eventId = eventId;
  }
}
