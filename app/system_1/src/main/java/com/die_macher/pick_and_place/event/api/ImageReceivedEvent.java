package com.die_macher.pick_and_place.event.api;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.awt.image.BufferedImage;

@Getter
public class ImageReceivedEvent extends ApplicationEvent {
    private final BufferedImage image;
    private final int cubeId;

    public ImageReceivedEvent(Object source, BufferedImage image, int cubeId) {
        super(source);
        this.image = image;
        this.cubeId = cubeId;
    }
}