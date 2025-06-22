package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.model.PickAndPlaceResult;
import com.die_macher.pick_and_place.service.api.PickAndPlaceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PickAndPlaceServiceImpl implements PickAndPlaceService {
  private final PickAndPlaceOrchestrator orchestrator;

  public PickAndPlaceServiceImpl(PickAndPlaceOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @Override
  public List<PickAndPlaceResult> startPickAndPlace(int cubeStackCount) {
    return orchestrator.startPickAndPlace(cubeStackCount);
  }
}
