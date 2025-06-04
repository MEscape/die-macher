package com.die_macher.pick_and_place.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovementServiceImpl Tests")
class PickAndPlaceServiceTest {

  @Mock private PickAndPlaceOrchestrator orchestrator;

  private PickAndPlaceServiceImpl movementService;

  @BeforeEach
  void setUp() {
    movementService = new PickAndPlaceServiceImpl(orchestrator);
  }

  @Test
  @DisplayName("Should delegate startPickAndPlace call to orchestrator with positive cube count")
  void shouldDelegateStartPickAndPlaceWithPositiveCubeCount() {
    // Given
    int cubeStackCount = 5;

    // When
    movementService.startPickAndPlace(cubeStackCount);

    // Then
    verify(orchestrator, times(1)).startPickAndPlace(cubeStackCount);
    verifyNoMoreInteractions(orchestrator);
  }
}
