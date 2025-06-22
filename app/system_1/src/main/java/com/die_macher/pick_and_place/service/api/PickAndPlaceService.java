package com.die_macher.pick_and_place.service.api;

import com.die_macher.pick_and_place.model.PickAndPlaceResult;

import java.util.List;

public interface PickAndPlaceService {
  /**
   * Start the pick and place process for the specified number of cubes
   *
   * @param cubeStackCount The number of cubes to process
   */
  List<PickAndPlaceResult> startPickAndPlace(int cubeStackCount);
}
