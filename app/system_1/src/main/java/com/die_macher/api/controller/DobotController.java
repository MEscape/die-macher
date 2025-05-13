package com.die_macher.api.controller;

import com.die_macher.dobot.service.DobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DobotController {
    private final DobotService dobotService;

    @Autowired
    public DobotController(DobotService dobotService) {
        this.dobotService = dobotService;
    }

    @GetMapping("/name")
    public String getDeviceName() {
        return dobotService.getDeviceName();
    }
}
