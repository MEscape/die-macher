package com.die_macher.tcp_server.model;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
public class PickAndPlaceResult {

    private LocalDateTime timestamp;

    private String color;

    private double temperature;

    private double humidity;

    private double energyCost;

    private boolean result;

}