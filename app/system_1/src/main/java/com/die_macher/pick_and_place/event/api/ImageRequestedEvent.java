package com.die_macher.pick_and_place.event.api;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ImageRequestedEvent extends ApplicationEvent {
    private final int cubeId;

    public ImageRequestedEvent(Object source, int cubeId) {
        super(source);
        this.cubeId = cubeId;
    }
}