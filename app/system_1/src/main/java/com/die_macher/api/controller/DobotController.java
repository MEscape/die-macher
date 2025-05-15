package com.die_macher.api.controller;

import com.die_macher.dobot.service.DobotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/move")
    public ResponseEntity<String> moveDobot(@RequestBody MoveRequest moveRequest) {
        boolean success = dobotService.moveToPosition(
                moveRequest.x(),
                moveRequest.y(),
                moveRequest.z(),
                moveRequest.r()
        );

        if (success) {
            return ResponseEntity.ok("Movement command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send movement command to Dobot. Possible communication error.");
        }
    }

    @PostMapping("/home")
    public ResponseEntity<String> homeDobot() {
        boolean success = dobotService.goHome();

        if (success) {
            return ResponseEntity.ok("Home command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send home command to Dobot. Ensure the device is connected and responsive.");
        }
    }

    @PostMapping("/suck")
    public ResponseEntity<String> suckDobot(@RequestBody SuckRequest suckRequest) {
        boolean success = dobotService.setVacuumState(suckRequest.isSucked());

        if (success) {
            return ResponseEntity.ok("Suck command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send vacuum command to Dobot. Check the end effector connection.");
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<String> executeQueue() {
        boolean success = dobotService.executeQueue();

        if (success) {
            return ResponseEntity.ok("Queue command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to execute the command queue on Dobot. Verify the queue contents and connection.");
        }
    }

    @PostMapping("/default-home")
    public ResponseEntity<String> defaultHomeDobot() {
        boolean success = dobotService.setDefaultHome();

        if (success) {
            return ResponseEntity.ok("Default home command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send default home command to Dobot. Check communication and device state.");
        }
    }

    @PostMapping("/name")
    public ResponseEntity<String> setDeviceName(@RequestParam String deviceName) {
        boolean success = dobotService.setDeviceName(deviceName);

        if (success) {
            return ResponseEntity.ok("Device name successfully updated to: " + deviceName);
        } else {
            return ResponseEntity.internalServerError().body("Failed to update device name. Check communication and device state.");
        }
    }

    @PostMapping("/movement-config")
    public ResponseEntity<String> setMovementConfig(@RequestBody MovementConfigRequest movementConfigRequest) {
        boolean success = dobotService.setMovementConfig(
                movementConfigRequest.xyzVelocity,
                movementConfigRequest.rVelocity,
                movementConfigRequest.xyzAcceleration,
                movementConfigRequest.rAcceleration
        );

        if (success) {
            return ResponseEntity.ok(String.format("""
                            Movement configuration successfully updated:
                            - XYZ Velocity: %.2f mm/s
                            - R Velocity: %.2f °/s
                            - XYZ Acceleration: %.2f mm/s²
                            - R Acceleration: %.2f °/s²""",
                    movementConfigRequest.xyzVelocity,
                    movementConfigRequest.rVelocity,
                    movementConfigRequest.xyzAcceleration,
                    movementConfigRequest.rAcceleration));
        } else {
            return ResponseEntity.internalServerError().body("Failed to update movement configuration. Check communication and device state.");
        }
    }

    @GetMapping("/movement-config")
    public ResponseEntity<String> getMovementConfig() {
        float[] movementConfig = dobotService.getMovementConfig();

        if (movementConfig != null && movementConfig.length == 4) {
            return ResponseEntity.ok(String.format("""
                            Current Movement Configuration:
                            - XYZ Velocity: %.2f mm/s
                            - R Velocity: %.2f °/s
                            - XYZ Acceleration: %.2f mm/s²
                            - R Acceleration: %.2f °/s²""",
                    movementConfig[0],
                    movementConfig[1],
                    movementConfig[2],
                    movementConfig[3]));
        } else {
            return ResponseEntity.internalServerError().body("Failed to retrieve movement configuration. Check communication and device state.");
        }
    }

    public record SuckRequest(boolean isSucked) {}

    public record MoveRequest(float x, float y, float z, float r) {}

    public record MovementConfigRequest(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration) {}
}
