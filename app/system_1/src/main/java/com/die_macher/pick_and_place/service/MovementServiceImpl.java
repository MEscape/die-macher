package com.die_macher.pick_and_place.service;

import com.die_macher.common.util.Instruction;
import com.die_macher.common.util.InstructionRegistry;
import com.die_macher.dobot.service.DobotService;
import com.die_macher.pick_and_place.config.MovementProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovementServiceImpl implements MovementService {
    private final DobotService dobotService;
    private final MovementProperties movementProperties;
    private final ColorDetectionService colorDetectionService;

    private final InstructionRegistry registry;
    Map<String, Integer> colorStacks = new HashMap<>();

    private final static float ABSOLUTE_FLOOR = -66.0000F;
    private final static float CUBE_HEIGHT = 27.0000F;
    private final static float OFFSET = 4.0000F;

    @Autowired
    public MovementServiceImpl(
            DobotService dobotService,
            MovementProperties movementProperties,
            ColorDetectionService colorDetectionService,
            InstructionRegistry registry

    ) {
        this.dobotService = dobotService;
        this.movementProperties = movementProperties;
        this.colorDetectionService = colorDetectionService;
        this.registry = registry;
    }

    @PostConstruct
    public void initialize() {
        //TODO set home and common params go home

        colorStacks.put("red", 0);
        colorStacks.put("green", 0);
        colorStacks.put("blue", 0);
        colorStacks.put("yellow", 0);

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

            Thread.sleep(5000);

            // final String color = detectDominantColor(image)
            // registry.execute("place_" + color);
            // colorStacks.put(color, colorStacks.getOrDefault(color, 0) + 1);
        }
    }
}