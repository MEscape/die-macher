package com.die_macher.pick_and_place.service;

import com.die_macher.common.util.Instruction;
import com.die_macher.common.util.InstructionRegistry;
import com.die_macher.dobot.service.DobotService;
import com.die_macher.pick_and_place.config.MovementProperties;
import com.die_macher.tcp.OutboundEndpoint;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MovementServiceImpl implements MovementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MovementServiceImpl.class);
    
    private final DobotService dobotService;
    private final MovementProperties movementProperties;
    private final OutboundEndpoint outboundEndpoint;
    private final InstructionRegistry registry;
    
    // Used to store the detected color from the TCP communication
    private final AtomicReference<String> detectedColorRef = new AtomicReference<>();
    // Used to signal when color detection is complete
    private CompletableFuture<String> colorDetectionFuture;
    
    Map<String, Integer> colorStacks = new HashMap<>();

    private final static float ABSOLUTE_FLOOR = -66.0000F;
    private final static float CUBE_HEIGHT = 26.0000F;
    private final static float OFFSET = 2.0000F;

    @Autowired
    public MovementServiceImpl(
            DobotService dobotService,
            MovementProperties movementProperties,
            OutboundEndpoint outboundEndpoint,
            InstructionRegistry registry
    ) {
        this.dobotService = dobotService;
        this.movementProperties = movementProperties;
        this.outboundEndpoint = outboundEndpoint;
        this.registry = registry;
    }

    @PostConstruct
    public void initialize() {
        //TODO set home and common params go home

        colorStacks.put("RED", 0);
        colorStacks.put("GREEN", 0);
        colorStacks.put("BLUE", 0);
        colorStacks.put("YELLOW", 0);

        List<Instruction> startSequence = new ArrayList<>();
        startSequence.add(args -> dobotService.stopExecuteQueue());
        startSequence.add(args -> dobotService.clearQueue());
        startSequence.add(args -> dobotService.setDefaultHome());
        startSequence.add(args -> fastMovement().run());
        startSequence.add(args -> dobotService.goHome());
        startSequence.add(args -> dobotService.executeQueue());

        registry.register("start", startSequence);

        // --- Inspect Cube Sequence ---
        List<Instruction> inspectSequence = new ArrayList<>();
        inspectSequence.add(args -> dobotService.stopExecuteQueue());
        inspectSequence.add(args -> dobotService.clearQueue());
        inspectSequence.add(args -> fastMovement().run());
        inspectSequence.add(args -> {
            float startHeightWithOffset = (float) args[0];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getStartPoint().getX(),
                    movementProperties.getPositions().getStartPoint().getY(),
                    startHeightWithOffset,
                    movementProperties.getPositions().getStartPoint().getR()
            );
        });
        inspectSequence.add(args -> slowMovement().run());
        inspectSequence.add(args -> {
            float stackTopHeight = (float) args[1];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getStartPoint().getX(),
                    movementProperties.getPositions().getStartPoint().getY(),
                    stackTopHeight,
                    movementProperties.getPositions().getStartPoint().getR()
            );
        });
        inspectSequence.add(args -> dobotService.setVacuumState(true));
        inspectSequence.add(args -> {
            float liftHeight = (float) args[2];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getStartPoint().getX(),
                    movementProperties.getPositions().getStartPoint().getY(),
                    liftHeight,
                    movementProperties.getPositions().getStartPoint().getR()
            );
        });
        inspectSequence.add(args -> fastMovement().run());
        inspectSequence.add(args -> dobotService.moveToPosition(
                movementProperties.getPositions().getCamera().getX(),
                movementProperties.getPositions().getCamera().getY(),
                movementProperties.getPositions().getCamera().getZ(),
                movementProperties.getPositions().getCamera().getR()
        ));
        inspectSequence.add(args -> dobotService.executeQueue());

        registry.register("inspect_cube", inspectSequence);

        // --- Green Cube Storage Location ---
        List<Instruction> greenSequence = new ArrayList<>();
        greenSequence.add(args -> dobotService.stopExecuteQueue());
        greenSequence.add(args -> dobotService.clearQueue());
        greenSequence.add(args -> fastMovement().run());
        greenSequence.add(args -> {
            float startHeightWithOffset = (float) args[0];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getGreen().getX(),
                    movementProperties.getPositions().getGreen().getY(),
                    startHeightWithOffset,
                    movementProperties.getPositions().getGreen().getR()
            );
        });
        greenSequence.add(args -> slowMovement().run());
        greenSequence.add(args -> {
            float stackTopHeight = (float) args[1];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getGreen().getX(),
                    movementProperties.getPositions().getGreen().getY(),
                    stackTopHeight,
                    movementProperties.getPositions().getGreen().getR()
            );
        });
        greenSequence.add(args -> dobotService.setVacuumState(false));
        greenSequence.add(args -> {
            float liftHeight = (float) args[2];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getGreen().getX(),
                    movementProperties.getPositions().getGreen().getY(),
                    liftHeight,
                    movementProperties.getPositions().getGreen().getR()
            );
        });
        greenSequence.add(args -> fastMovement().run());
        greenSequence.add(args -> {
            dobotService.moveToPosition(
                    137.8012F,
                    148.6876F,
                    29.1770F,
                    0.0F
            );
        });
        greenSequence.add(args -> dobotService.executeQueue());

        registry.register("place_GREEN", greenSequence);

        // --- Red Cube Storage Location ---
        List<Instruction> redSequence = new ArrayList<>();
        redSequence.add(args -> dobotService.stopExecuteQueue());
        redSequence.add(args -> dobotService.clearQueue());
        redSequence.add(args -> fastMovement().run());
        redSequence.add(args -> {
            float startHeightWithOffset = (float) args[0];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getRed().getX(),
                    movementProperties.getPositions().getRed().getY(),
                    startHeightWithOffset,
                    movementProperties.getPositions().getRed().getR()
            );
        });
        redSequence.add(args -> slowMovement().run());
        redSequence.add(args -> {
            float stackTopHeight = (float) args[1];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getRed().getX(),
                    movementProperties.getPositions().getRed().getY(),
                    stackTopHeight,
                    movementProperties.getPositions().getRed().getR()
            );
        });
        redSequence.add(args -> dobotService.setVacuumState(false));
        redSequence.add(args -> {
            float liftHeight = (float) args[2];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getRed().getX(),
                    movementProperties.getPositions().getRed().getY(),
                    liftHeight,
                    movementProperties.getPositions().getRed().getR()
            );
        });
        redSequence.add(args -> fastMovement().run());
        redSequence.add(args -> {
            dobotService.moveToPosition(137.8012F, 148.6876F, 29.1770F, 0.0F);
        });
        redSequence.add(args -> dobotService.executeQueue());

        registry.register("place_RED", redSequence);

        // --- Yellow Cube Storage Location ---
        List<Instruction> yellowSequence = new ArrayList<>();
        yellowSequence.add(args -> dobotService.stopExecuteQueue());
        yellowSequence.add(args -> dobotService.clearQueue());
        yellowSequence.add(args -> fastMovement().run());
        yellowSequence.add(args -> {
            float startHeightWithOffset = (float) args[0];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getYellow().getX(),
                    movementProperties.getPositions().getYellow().getY(),
                    startHeightWithOffset,
                    movementProperties.getPositions().getYellow().getR()
            );
        });
        yellowSequence.add(args -> slowMovement().run());
        yellowSequence.add(args -> {
            float stackTopHeight = (float) args[1];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getYellow().getX(),
                    movementProperties.getPositions().getYellow().getY(),
                    stackTopHeight,
                    movementProperties.getPositions().getYellow().getR()
            );
        });
        yellowSequence.add(args -> dobotService.setVacuumState(false));
        yellowSequence.add(args -> {
            float liftHeight = (float) args[2];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getYellow().getX(),
                    movementProperties.getPositions().getYellow().getY(),
                    liftHeight,
                    movementProperties.getPositions().getYellow().getR()
            );
        });
        yellowSequence.add(args -> fastMovement().run());
        yellowSequence.add(args -> {
            dobotService.moveToPosition(137.8012F, 148.6876F, 29.1770F, 0.0F);
        });
        yellowSequence.add(args -> dobotService.executeQueue());

        registry.register("place_YELLOW", yellowSequence);

        // --- Blue Cube Storage Location ---
        List<Instruction> blueSequence = new ArrayList<>();
        blueSequence.add(args -> dobotService.stopExecuteQueue());
        blueSequence.add(args -> dobotService.clearQueue());
        blueSequence.add(args -> fastMovement().run());
        blueSequence.add(args -> {
            float startHeightWithOffset = (float) args[0];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getBlue().getX(),
                    movementProperties.getPositions().getBlue().getY(),
                    startHeightWithOffset,
                    movementProperties.getPositions().getBlue().getR()
            );
        });
        blueSequence.add(args -> slowMovement().run());
        blueSequence.add(args -> {
            float stackTopHeight = (float) args[1];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getBlue().getX(),
                    movementProperties.getPositions().getBlue().getY(),
                    stackTopHeight,
                    movementProperties.getPositions().getBlue().getR()
            );
        });
        blueSequence.add(args -> dobotService.setVacuumState(false));
        blueSequence.add(args -> {
            float liftHeight = (float) args[2];
            dobotService.moveToPosition(
                    movementProperties.getPositions().getBlue().getX(),
                    movementProperties.getPositions().getBlue().getY(),
                    liftHeight,
                    movementProperties.getPositions().getBlue().getR()
            );
        });
        blueSequence.add(args -> fastMovement().run());
        blueSequence.add(args -> {
            dobotService.moveToPosition(137.8012F, 148.6876F, 29.1770F, 0.0F);
        });
        blueSequence.add(args -> dobotService.executeQueue());

        registry.register("place_BLUE", blueSequence);
    }

    private Runnable fastMovement() {
        return () -> dobotService.setMovementConfig(
                movementProperties.getFast().getVelocity().getXyz(),
                movementProperties.getFast().getVelocity().getR(),
                movementProperties.getFast().getAcceleration().getXyz(),
                movementProperties.getFast().getAcceleration().getR()
        );
    }

    private Runnable slowMovement() {
        return () -> dobotService.setMovementConfig(
                movementProperties.getSlow().getVelocity().getXyz(),
                movementProperties.getSlow().getVelocity().getR(),
                movementProperties.getSlow().getAcceleration().getXyz(),
                movementProperties.getSlow().getAcceleration().getR()
        );
    }
    
    @Override
    public void startPickAndPlace(final int cubeStackCount) {
        registry.execute("start");

        for (int i = cubeStackCount; i > 0; i--) {
            float startHeightWithOffset = i * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR;
            float stackTopHeight = i * CUBE_HEIGHT + ABSOLUTE_FLOOR;
            float liftHeight = i * CUBE_HEIGHT + CUBE_HEIGHT + ABSOLUTE_FLOOR;

            registry.execute(
                    "inspect_cube",
                    startHeightWithOffset,
                    stackTopHeight,
                    liftHeight,
                    liftHeight
            );

            // Wait for the robot to stabilize (10 seconds)
            try {
                LOGGER.info("Waiting for robot to stabilize before taking image...");
                Thread.sleep(40000); // 10 second delay

                // Now that robot is stable, reset the future and request the image
                colorDetectionFuture = new CompletableFuture<>();
                boolean requestSent = outboundEndpoint.requestImage();
                if (!requestSent) {
                    LOGGER.error("Failed to send image request");
                    colorDetectionFuture.completeExceptionally(new RuntimeException("Failed to send image request"));
                }

                // Wait for color detection to complete (with timeout)
                String detectedColor = colorDetectionFuture.get(10, java.util.concurrent.TimeUnit.SECONDS);
                LOGGER.info("Detected color: {}", detectedColor);

                // Update the stack count for this color
                int colorCubeStackCount = colorStacks.getOrDefault(detectedColor, 0) + 1;
                colorStacks.put(detectedColor, colorCubeStackCount);
                registry.execute(
                        "place_" + detectedColor,
                        colorCubeStackCount * CUBE_HEIGHT + OFFSET + ABSOLUTE_FLOOR,
                        colorCubeStackCount * CUBE_HEIGHT + ABSOLUTE_FLOOR,
                        colorCubeStackCount * CUBE_HEIGHT + CUBE_HEIGHT + ABSOLUTE_FLOOR
                );
            } catch (Exception e) {
                LOGGER.error("Error during color detection: {}", e.getMessage(), e);
                // If color detection fails, place in a default position
                // registry.execute("place_RED"); // Default to RED if detection fails
            }
        }
    }

    /**
     * Process the detected color from the InboundEndpoint
     * This method is called by the InboundEndpoint when a color is detected
     *
     * @param color The detected dominant color
     */
    @Override
    public void processDetectedColor(String color) {
        LOGGER.info("Processing detected color: {}", color);
        detectedColorRef.set(color);

        // Complete the future to signal that color detection is complete
        if (colorDetectionFuture != null && !colorDetectionFuture.isDone()) {
            colorDetectionFuture.complete(color);
        }
    }
}