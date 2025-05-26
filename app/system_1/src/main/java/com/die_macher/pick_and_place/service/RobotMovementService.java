package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;
import com.die_macher.pick_and_place.dobot.service.api.DobotService;
import com.die_macher.pick_and_place.config.RobotConfiguration;
import com.die_macher.pick_and_place.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class RobotMovementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RobotMovementService.class);

    private final DobotService dobotService;
    private final RobotConfiguration config;
    private final HeightCalculator heightCalculator;

    public RobotMovementService(DobotService dobotService,
                                RobotConfiguration config,
                                HeightCalculator heightCalculator) {
        this.dobotService = dobotService;
        this.config = config;
        this.heightCalculator = heightCalculator;
    }

    public void initialize() {
        LOGGER.info("Initializing robot...");
        dobotService.stopExecuteQueue();
        dobotService.clearQueue();

        Position startPoint = config.positions().startPoint();
        dobotService.setDefaultHome(startPoint.x(), startPoint.y(), startPoint.z(), startPoint.r());

        setFastMovement();
        dobotService.goHome();
        dobotService.executeQueue();
    }

    public void pickupCube(int stackPosition) {
        LOGGER.info("Picking up cube from position {}", stackPosition);

        dobotService.stopExecuteQueue();
        dobotService.clearQueue();

        Position pickupPoint = config.positions().pickupPoint();

        // Fast approach
        setFastMovement();
        dobotService.moveToPosition(
                PTPModes.MOVJ_XYZ,
                pickupPoint.x(),
                pickupPoint.y(),
                heightCalculator.calculateApproachHeight(stackPosition),
                pickupPoint.r()
        );

        // Slow precise pickup
        setSlowMovement();
        dobotService.moveToPosition(
                PTPModes.MOVL_XYZ,
                pickupPoint.x(),
                pickupPoint.y(),
                heightCalculator.calculatePickupHeight(stackPosition),
                pickupPoint.r()
        );

        dobotService.setVacuumState(true);
    }

    public void moveToCamera() {
        LOGGER.info("Moving to camera position");

        setFastMovement();

        dobotService.setLiftHeight(
                config.physicalConstants().cubeHeight(),
                config.physicalConstants().maxHeight()
        );
        Position camera = config.positions().camera();
        dobotService.moveToPosition(
                PTPModes.JUMP_XYZ,
                camera.x(),
                camera.y(),
                camera.z(),
                camera.r()
        );

        dobotService.executeQueue();
    }

    public void placeCube(Color color, int stackHeight, int maxStackHeight) {
        LOGGER.info("Placing {} cube at stack height {}", color, stackHeight);

        dobotService.stopExecuteQueue();
        dobotService.clearQueue();

        setFastMovement();

        dobotService.setLiftHeight(
                heightCalculator.calculateLiftHeight(config.positions().camera().z(), maxStackHeight),
                config.physicalConstants().maxHeight()
        );

        Position targetPosition = getPositionForColor(color);
        dobotService.moveToPosition(
                PTPModes.JUMP_XYZ,
                targetPosition.x(),
                targetPosition.y(),
                heightCalculator.calculateApproachHeight(stackHeight),
                targetPosition.r()
        );

        // Slow precise placement
        setSlowMovement();
        dobotService.moveToPosition(
                PTPModes.MOVL_XYZ,
                targetPosition.x(),
                targetPosition.y(),
                heightCalculator.calculatePickupHeight(stackHeight),
                targetPosition.r()
        );

        dobotService.setVacuumState(false);

        // Return to neutral position
        setFastMovement();

        dobotService.setLiftHeight(
                heightCalculator.calculateLiftHeight(
                        heightCalculator.calculatePickupHeight(stackHeight),
                        maxStackHeight
                ),
                config.physicalConstants().maxHeight()
        );

        Position startPoint = config.positions().startPoint();
        dobotService.moveToPosition(
                PTPModes.JUMP_XYZ,
                startPoint.x(),
                startPoint.y(),
                startPoint.z(),
                startPoint.r()
        );

        dobotService.executeQueue();
    }

    private Position getPositionForColor(Color color) {
        if (Color.RED.equals(color)) {
            return config.positions().red();
        }

        if (Color.GREEN.equals(color)) {
            return config.positions().green();
        }

        if (Color.BLUE.equals(color)) {
            return config.positions().blue();
        }

        if (Color.YELLOW.equals(color)) {
            return config.positions().yellow();
        }

        throw new IllegalArgumentException("Unsupported color: " + color);
    }

    private void setFastMovement() {
        var fast = config.fastMovement();
        dobotService.setMovementConfig(
                fast.xyzVelocity(), fast.rVelocity(),
                fast.xyzAcceleration(), fast.rAcceleration()
        );
    }

    private void setSlowMovement() {
        var slow = config.slowMovement();
        dobotService.setMovementConfig(
                slow.xyzVelocity(), slow.rVelocity(),
                slow.xyzAcceleration(), slow.rAcceleration()
        );
    }
}