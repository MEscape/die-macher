package com.die_macher.pick_and_place.service;

import com.die_macher.pick_and_place.service.api.MovementService;
import org.springframework.stereotype.Service;

@Service
public class MovementServiceImpl implements MovementService {
    private final PickAndPlaceOrchestrator orchestrator;

    public MovementServiceImpl(PickAndPlaceOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Override
    public void startPickAndPlace(int cubeStackCount) {
        orchestrator.startPickAndPlace(cubeStackCount);
    }
}