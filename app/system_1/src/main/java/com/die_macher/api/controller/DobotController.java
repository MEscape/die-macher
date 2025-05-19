package com.die_macher.api.controller;

import com.die_macher.common.util.QueueManager;
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

    //working
    @GetMapping("/name")
    public String getDeviceName() {
        return dobotService.getDeviceName();
    }

    // working but not queued
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

    //working but not queued
    @PostMapping("/home")
    public ResponseEntity<String> homeDobot() {
        boolean success = dobotService.goHome();

        if (success) {
            return ResponseEntity.ok("Home command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send home command to Dobot. Ensure the device is connected and responsive.");
        }
    }

    // working but not queued
    @PostMapping("/suck")
    public ResponseEntity<String> suckDobot(@RequestBody SuckRequest suckRequest) {
        boolean success = dobotService.setVacuumState(suckRequest.isSucked());

        if (success) {
            return ResponseEntity.ok("Suck command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send vacuum command to Dobot. Check the end effector connection.");
        }
    }

    // not working
    @PostMapping("/execute")
    public ResponseEntity<String> executeQueue() {
        boolean success = dobotService.executeQueue();

        if (success) {
            return ResponseEntity.ok("Queue command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to execute the command queue on Dobot. Verify the queue contents and connection.");
        }
    }

    // not working
    @PostMapping("/default-home")
    public ResponseEntity<String> defaultHomeDobot() {
        boolean success = dobotService.setDefaultHome();

        if (success) {
            return ResponseEntity.ok("Default home command sent successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to send default home command to Dobot. Check communication and device state.");
        }
    }

    // working
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
                - R Acceleration: %.2f °/s²
                """,
                    movementConfigRequest.xyzVelocity,
                    movementConfigRequest.rVelocity,
                    movementConfigRequest.xyzAcceleration,
                    movementConfigRequest.rAcceleration
            ));

        } else {
            return ResponseEntity.internalServerError().body("Failed to update movement configuration. Check communication and device state.");
        }
    }

    @PostMapping("/clear-queue")
    public ResponseEntity<String> clearQueue() {
        boolean success = dobotService.clearQueue();

        if (success) {
            return ResponseEntity.ok("Device queue successfully cleared.");
        } else {
            return ResponseEntity.internalServerError().body("Failed to clear device queue. Check communication and device state.");
        }
    }

    @PostMapping("/stop-queue")
    public ResponseEntity<String> stopQueue() {
        boolean success = dobotService.stopExecuteQueue();

        if (success) {
            return ResponseEntity.ok("Device queue successfully cleared.");
        } else {
            return ResponseEntity.internalServerError().body("Failed to clear device queue. Check communication and device state.");
        }
    }

    @GetMapping("/pick-and-place")
    public ResponseEntity<String> pickAndPlace() {
        QueueManager<String> manager = new QueueManager<>();
        manager.addQueue("cameraMovement");

        // Enqueue commands (as Runnable)
        manager.enqueue("cameraMovement", dobotService::stopExecuteQueue);
        manager.enqueue("cameraMovement", () -> dobotService.moveToPosition(283.9522F, 10.8680F, -40.3899F, 55.7961F));
        manager.enqueue("cameraMovement", () -> dobotService.setVacuumState(true));
        manager.enqueue("cameraMovement", () -> dobotService.moveToPosition(139.1296F, -267.4972F, -39.6540F, 4.2960F));
        manager.enqueue("cameraMovement", () -> dobotService.setVacuumState(false));
        manager.enqueue("cameraMovement", dobotService::goHome);
        manager.enqueue("cameraMovement", dobotService::executeQueue);

        // Execute the queued commands one by one
        while (!manager.isQueueEmpty("cameraMovement")) {
            manager.executeNext("cameraMovement");
        }

        return ResponseEntity.ok("Commands executed in order.");
    }

    public record SuckRequest(boolean isSucked) {}

    public record MoveRequest(float x, float y, float z, float r) {}

    public record MovementConfigRequest(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration, float jointVelocity, float jointAcceleration, float commonVelocityRatio, float commonAccelerationRatio) {}
}
