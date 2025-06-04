package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.service.api.PickAndPlaceService;
import org.springframework.stereotype.Service;

@Service
public class PickAndPlaceServiceImpl implements PickAndPlaceService {
  private final PickAndPlaceOrchestrator orchestrator;

  public PickAndPlaceServiceImpl(PickAndPlaceOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @Override
  public void startPickAndPlace(int cubeStackCount) {
    orchestrator.startPickAndPlace(cubeStackCount);
  }
}
