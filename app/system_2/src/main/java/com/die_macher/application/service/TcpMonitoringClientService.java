package com.die_macher.application.service;

import com.die_macher.application.event.TcpConnectionRegistry;
import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.port.inbound.TcpMonitoringClientProvider;
import com.die_macher.infrastructure.adapter.web.dto.CubeManipulationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TcpMonitoringClientService implements TcpMonitoringClientProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(TcpMonitoringClientService.class);

  private final TcpConnectionRegistry registry;
  private final ObjectMapper mapper;

  public TcpMonitoringClientService(TcpConnectionRegistry registry, ObjectMapper objectMapper) {
    this.registry = registry;
    this.mapper = objectMapper;
  }

  @Override
  public void sendCubeProcessedEvent(int cubeCount) {
    try {
      ObjectNode json = mapper.createObjectNode();
      json.put("event", "CUBE_PROCESSED");
      json.put("cubeCount", cubeCount);

      byte[] payload = mapper.writeValueAsString(json).getBytes(StandardCharsets.UTF_8);
      registry.send(payload);
    } catch (Exception e) {
      LOGGER.error(
          "Failed to send cube processed event with count {}: {}", cubeCount, e.getMessage(), e);
      throw new DataProcessingException(
          "Failed to send cube processed event with count " + cubeCount, e);
    }
  }

  @Override
  public void sendCubeManipulation(String color, CubeManipulationRequest.Action action) {
    try {
      ObjectNode json = mapper.createObjectNode();
      json.put("event", "CUBE_MANIPULATION");
      json.put("color", color.toUpperCase());
      json.put("action", action.name());

      byte[] payload = mapper.writeValueAsString(json).getBytes(StandardCharsets.UTF_8);
      registry.send(payload);
    } catch (Exception e) {
      LOGGER.error("Failed to send cube manipulation event: {} {}", color, action, e);
      throw new DataProcessingException("Failed to send cube manipulation event", e);
    }
  }
}
