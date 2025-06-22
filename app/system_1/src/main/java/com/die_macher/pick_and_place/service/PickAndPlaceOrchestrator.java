package com.die_macher.pick_and_place.service;

import com.die_macher.awattar.service.AwattarService;
import com.die_macher.opcua_client.OpcuaClientApplication;
import com.die_macher.opcua_client.SensorData;
import com.die_macher.pick_and_place.event.api.ImageReceivedEvent;
import com.die_macher.pick_and_place.event.api.ImageRequestedEvent;
import com.die_macher.pick_and_place.model.PickAndPlaceResult;
import com.die_macher.pick_and_place.model.StackInfo;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class PickAndPlaceOrchestrator {
  private static final Logger LOGGER = LoggerFactory.getLogger(PickAndPlaceOrchestrator.class);
  private static final int COLOR_DETECTION_TIMEOUT_SECONDS = 10;
  private static final int CAMERA_STABILIZATION_TIME_MS = 10000;
  private static final int RETURN_STABILIZATION_TIME_MS = 10000;
  private static final int INITIAL_STABILIZATION_TIME_MS = 25000;

  private final RobotMovementService robotMovementService;
  private final StackTracker stackTracker;
  private final ApplicationEventPublisher eventPublisher;
  private final ColorDetectionService colorDetectionService;
  private final OpcuaClientApplication opcuaClientApplication;
  private final AwattarService  awattarService;
  private final AtomicInteger eventIdCounter = new AtomicInteger(1);  // Track pending color detections
  private final ConcurrentHashMap<Integer, CompletableFuture<Color>> pendingDetections =
      new ConcurrentHashMap<>();

  @Autowired
  public PickAndPlaceOrchestrator(
          RobotMovementService robotMovementService,
          StackTracker stackTracker,
          ColorDetectionService colorDetectionService,
          ApplicationEventPublisher eventPublisher, OpcuaClientApplication opcuaClientApplication, AwattarService awattarService) {
    this.robotMovementService = robotMovementService;
    this.stackTracker = stackTracker;
    this.eventPublisher = eventPublisher;
    this.colorDetectionService = colorDetectionService;
    this.opcuaClientApplication = opcuaClientApplication;
    this.awattarService = awattarService;
  }

  public List<PickAndPlaceResult> startPickAndPlace(int cubeStackCount) {
    LOGGER.info("Starting pick and place operation for {} cubes", cubeStackCount);
    List<PickAndPlaceResult> results = new ArrayList<>();

    try {
      robotMovementService.initialize();
      stackTracker.reset();
      Thread.sleep(INITIAL_STABILIZATION_TIME_MS);

      for (int cubePosition = cubeStackCount; cubePosition > 0; cubePosition--) {
        PickAndPlaceResult result = processCube(cubePosition);
        results.add(result);
      }

      LOGGER.info("Pick and place operation completed successfully");
    } catch (Exception e) {
      throw new RuntimeException("Pick and place operation failed", e);
    }
    return results;
  }

  private PickAndPlaceResult processCube(int cubePosition) {
    try {
      LOGGER.info("Processing cube at position {}", cubePosition);

      // Pick up the cube
      robotMovementService.pickupCube(cubePosition);

      // Move to camera for inspection
      robotMovementService.moveToCamera();

      // Wait for robot to stabilize
      Thread.sleep(CAMERA_STABILIZATION_TIME_MS);

      // Request color detection
      Color detectedColor = requestColorDetection();

      // Update stack and place cube
      StackInfo stackInfo = stackTracker.addCube(detectedColor);
      robotMovementService.placeCube(
          detectedColor,
          stackInfo.currentHeight(),
          Math.max(stackTracker.getMaxStackHeight(), cubePosition));

      // Wait for robot to go to init position
      Thread.sleep(RETURN_STABILIZATION_TIME_MS);
      Optional<SensorData> optionalData = opcuaClientApplication.getLatestSensorData();

      double energyCost = awattarService.getCurrentPartCost();

      if (optionalData.isEmpty()) {
        throw new RuntimeException("Sensor data not available.");
      }

      SensorData data = optionalData.get();
      return new PickAndPlaceResult(
              detectedColor.toString(),
              data.getTemperature(),
              data.getHumidity(),
              energyCost,
              true
      );
    } catch (Exception e) {
      throw new RuntimeException("Failed to process cube", e);
    }
  }

  private Color requestColorDetection() {
    CompletableFuture<Color> colorFuture = new CompletableFuture<>();
    int eventId = eventIdCounter.getAndIncrement();
    pendingDetections.put(eventId, colorFuture);

    // Publish event to request color detection
    eventPublisher.publishEvent(new ImageRequestedEvent(this, eventId));

    try {
      return colorFuture.get(COLOR_DETECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception e) {
      pendingDetections.remove(eventId);
      throw new RuntimeException("Color detection timeout or failed for cube " + eventId, e);
    }
  }

  @EventListener
  public void handleColorDetected(ImageReceivedEvent event) {
    Color detectedColor = colorDetectionService.detectDominantColor(event.getImage());
    LOGGER.info("Color detected: {} for cube {}", detectedColor, event.getEventId());

    CompletableFuture<Color> future = pendingDetections.remove(event.getEventId());
    if (future != null && !future.isDone()) {
      future.complete(detectedColor);
    }
  }


}
