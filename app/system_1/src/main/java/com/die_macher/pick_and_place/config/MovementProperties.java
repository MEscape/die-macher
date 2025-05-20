package com.die_macher.pick_and_place.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "dobot.movement")
public class MovementProperties {
    @Valid
    private Profile fast;

    @Valid
    private Profile slow;

    @Valid
    private Positions positions;

    @Getter
    @Setter
    public static class Profile {
        private AxisProperties velocity;
        private AxisProperties acceleration;
    }

    @Getter
    @Setter
    public static class AxisProperties {
        @NotBlank
        @Min(1)
        @Max(1000)
        private int xyz;

        @NotBlank
        @Min(1)
        @Max(1000)
        private int r;
    }

    @Getter
    @Setter
    public static class Positions {
        private Position startPoint;
        private Position camera;
        private Position yellow;
        private Position green;
        private Position blue;
        private Position red;
    }

    @Getter
    @Setter
    public static class Position {
        @NotBlank
        private float x;

        @NotBlank
        private float y;

        @NotBlank
        private float z;

        @NotBlank
        private float r;
    }
}