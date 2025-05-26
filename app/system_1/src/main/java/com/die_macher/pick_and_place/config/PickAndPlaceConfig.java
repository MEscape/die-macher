package com.die_macher.pick_and_place.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RobotConfiguration.class)
public class PickAndPlaceConfig {
}
