package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.dobot.protocol.api.PTPModes;
import com.die_macher.pick_and_place.dobot.service.api.DobotService;
import com.die_macher.pick_and_place.config.RobotConfiguration;
import com.die_macher.pick_and_place.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RobotMovementService Tests")
class RobotMovementServiceTest {

    @Mock
    private DobotService dobotService;

    @Mock
    private RobotConfiguration config;

    @Mock
    private HeightCalculator heightCalculator;

    @Mock
    private RobotConfiguration.RobotPositions positions;

    @Mock
    private RobotConfiguration.MovementProfile fastMovement;

    @Mock
    private RobotConfiguration.MovementProfile slowMovement;

    private RobotMovementService robotMovementService;

    @Mock
    private RobotConfiguration.PhysicalConstants physicalConstants;

    private final Position startPoint = new Position(0, 0, 100, 0);
    private final Position pickupPoint = new Position(150, 0, 50, 0);
    private final Position cameraPoint = new Position(100, 100, 80, 45);
    private final Position redPosition = new Position(200, 50, 20, 0);

    @BeforeEach
    void setUp() {
        robotMovementService = new RobotMovementService(dobotService, config, heightCalculator);
    }

    @Test
    @DisplayName("Should initialize robot correctly")
    void shouldInitializeRobot() {
        // When
        when(config.positions()).thenReturn(positions);
        when(config.fastMovement()).thenReturn(fastMovement);
        when(positions.startPoint()).thenReturn(startPoint);

        robotMovementService.initialize();

        // Then
        verify(dobotService).stopExecuteQueue();
        verify(dobotService).clearQueue();
        verify(dobotService).setDefaultHome(startPoint.x(), startPoint.y(), startPoint.z(), startPoint.r());
        verify(dobotService).setMovementConfig(
                fastMovement.xyzVelocity(), fastMovement.rVelocity(),
                fastMovement.xyzAcceleration(), fastMovement.rAcceleration()
        );
        verify(dobotService).goHome();
        verify(dobotService).executeQueue();
    }

    @Test
    @DisplayName("Should pickup cube correctly")
    void shouldPickupCube() {
        // Given
        when(config.positions()).thenReturn(positions);
        when(config.fastMovement()).thenReturn(fastMovement);
        when(config.slowMovement()).thenReturn(slowMovement);
        when(positions.pickupPoint()).thenReturn(pickupPoint);

        int stackPosition = 2;
        when(heightCalculator.calculateApproachHeight(stackPosition)).thenReturn(50.0f);
        when(heightCalculator.calculatePickupHeight(stackPosition)).thenReturn(20.0f);

        // When
        robotMovementService.pickupCube(stackPosition);

        // Then
        verify(dobotService, times(2)).setMovementConfig(anyFloat(), anyFloat(), anyFloat(), anyFloat());
        verify(dobotService, times(1)).moveToPosition(eq(PTPModes.MOVL_XYZ), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        verify(dobotService, times(1)).moveToPosition(eq(PTPModes.MOVJ_XYZ), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        verify(dobotService).setVacuumState(true);
    }

    @Test
    @DisplayName("Should move to camera correctly")
    void shouldMoveToCamera() {
        // When
        when(config.positions()).thenReturn(positions);
        when(config.fastMovement()).thenReturn(fastMovement);
        when(positions.camera()).thenReturn(cameraPoint);

        when(config.physicalConstants()).thenReturn(physicalConstants);
        when(physicalConstants.maxHeight()).thenReturn(200.0f);

        robotMovementService.moveToCamera();

        // Then
        verify(dobotService).setMovementConfig(
                fastMovement.xyzVelocity(), fastMovement.rVelocity(),
                fastMovement.xyzAcceleration(), fastMovement.rAcceleration()
        );
        verify(dobotService).moveToPosition(PTPModes.JUMP_XYZ, cameraPoint.x(), cameraPoint.y(), cameraPoint.z(), cameraPoint.r());
        verify(dobotService).executeQueue();
    }

    @Test
    @DisplayName("Should place cube for red color")
    void shouldPlaceCubeForRedColor() {
        // Given
        when(config.positions()).thenReturn(positions);
        when(config.fastMovement()).thenReturn(fastMovement);
        when(config.slowMovement()).thenReturn(slowMovement);
        when(positions.startPoint()).thenReturn(startPoint);
        when(positions.red()).thenReturn(redPosition);

        Color color = Color.RED;
        int stackHeight = 1;
        int maxStackHeight = 2;
        when(heightCalculator.calculateApproachHeight(stackHeight)).thenReturn(30.0f);
        when(heightCalculator.calculatePickupHeight(stackHeight)).thenReturn(10.0f);
        when(config.physicalConstants()).thenReturn(physicalConstants);
        when(physicalConstants.maxHeight()).thenReturn(200.0f);
        when(positions.camera()).thenReturn(cameraPoint);

        // When
        robotMovementService.placeCube(color, stackHeight, maxStackHeight);

        // Then
        verify(dobotService).stopExecuteQueue();
        verify(dobotService).clearQueue();
        verify(dobotService).setVacuumState(false);
        verify(dobotService, times(1)).moveToPosition(eq(PTPModes.MOVL_XYZ), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        verify(dobotService, times(2)).moveToPosition(eq(PTPModes.JUMP_XYZ), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        verify(dobotService).executeQueue();
    }

    @Test
    @DisplayName("Should handle all supported colors")
    void shouldHandleAllSupportedColors() {
        // Given
        when(config.positions()).thenReturn(positions);
        when(config.fastMovement()).thenReturn(fastMovement);
        when(config.slowMovement()).thenReturn(slowMovement);
        when(positions.startPoint()).thenReturn(startPoint);
        when(positions.red()).thenReturn(redPosition);
        when(positions.camera()).thenReturn(cameraPoint);

        when(positions.green()).thenReturn(new Position(200, 100, 20, 0));
        when(positions.blue()).thenReturn(new Position(200, 150, 20, 0));
        when(positions.yellow()).thenReturn(new Position(200, 200, 20, 0));
        when(heightCalculator.calculateApproachHeight(anyInt())).thenReturn(30.0f);
        when(heightCalculator.calculatePickupHeight(anyInt())).thenReturn(10.0f);
        when(config.physicalConstants()).thenReturn(physicalConstants);
        when(physicalConstants.maxHeight()).thenReturn(200.0f);

        // When & Then
        robotMovementService.placeCube(Color.RED, 0, 1);
        robotMovementService.placeCube(Color.GREEN, 0, 1);
        robotMovementService.placeCube(Color.BLUE, 0, 1);
        robotMovementService.placeCube(Color.YELLOW, 0, 1);

        verify(dobotService, times(4)).executeQueue();
    }
}