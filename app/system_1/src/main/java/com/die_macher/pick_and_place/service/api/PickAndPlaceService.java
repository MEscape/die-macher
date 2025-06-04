package com.die_macher.pick_and_place.service.api;

public interface PickAndPlaceService {
  /**
   * Start the pick and place process for the specified number of cubes
   *
   * @param cubeStackCount The number of cubes to process
   */
  void startPickAndPlace(int cubeStackCount);
}
