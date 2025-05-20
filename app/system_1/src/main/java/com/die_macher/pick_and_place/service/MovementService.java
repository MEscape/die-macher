package com.die_macher.pick_and_place.service;

public interface MovementService {
    /**
     * Start the pick and place process for the specified number of cubes
     * 
     * @param cubeStackCount The number of cubes to process
     */
    void startPickAndPlace(int cubeStackCount);
    
    /**
     * Process the detected color from the InboundEndpoint
     * This method is called by the InboundEndpoint when a color is detected
     * 
     * @param color The detected dominant color
     */
    void processDetectedColor(String color);
}
