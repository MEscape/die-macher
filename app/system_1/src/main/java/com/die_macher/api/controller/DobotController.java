package com.die_macher.api.controller;

import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;
import com.die_macher.pick_and_place.dobot.service.api.DobotService;
import com.die_macher.pick_and_place.event.api.ImageRequestedEvent;
import com.die_macher.pick_and_place.service.api.PickAndPlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DobotController {
    private final DobotService dobotService;
    private final PickAndPlaceService pickAndPlaceService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public DobotController(DobotService dobotService, PickAndPlaceService pickAndPlaceService, ApplicationEventPublisher eventPublisher) {
        this.dobotService = dobotService;
        this.pickAndPlaceService = pickAndPlaceService;
        this.eventPublisher = eventPublisher;
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
                PTPModes.MOVJ_XYZ,
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
        boolean success = dobotService.setDefaultHome(137.8012F, 148.6876F, 29.1770F, 0.0F);

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

    //MAX 5
    @GetMapping("/pickAndPlace")
    public void pickAndPlace() {
        pickAndPlaceService.startPickAndPlace(5);
    }

    @GetMapping("/color")
    public void color() {
        eventPublisher.publishEvent(new ImageRequestedEvent(this, 1));
    }

    public record SuckRequest(boolean isSucked) {}

    public record MoveRequest(float x, float y, float z, float r) {}

    public record MovementConfigRequest(float xyzVelocity, float rVelocity, float xyzAcceleration, float rAcceleration, float jointVelocity, float jointAcceleration, float commonVelocityRatio, float commonAccelerationRatio) {}
}
