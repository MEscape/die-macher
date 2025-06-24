package com.die_macher.application.service;

import com.die_macher.application.tcp_client.TcpConnectionRegistry;
import com.die_macher.application.tcp_client.TcpMessageSender;
import com.die_macher.application.transformer.PriceDataTransformer;
import com.die_macher.domain.exception.DataProcessingException;
import com.die_macher.domain.model.price.PriceData;
import com.die_macher.domain.port.inbound.TcpMonitoringClientProvider;
import com.die_macher.infrastructure.adapter.web.dto.CubeManipulationRequest;
import com.die_macher.infrastructure.adapter.web.dto.PriceDataResponse;
import com.die_macher.infrastructure.adapter.web.dto.TomorrowDataRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TcpMonitoringClientService implements TcpMonitoringClientProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(TcpMonitoringClientService.class);

  private final TcpMessageSender tcpMessageSender;
  private final ObjectMapper mapper;
  private final PriceDataTransformer priceDataTransformer;

  public TcpMonitoringClientService(TcpMessageSender tcpMessageSender, ObjectMapper objectMapper, PriceDataTransformer priceDataTransformer) {
      this.tcpMessageSender = tcpMessageSender;
      this.mapper = objectMapper;
      this.priceDataTransformer = priceDataTransformer;
  }

  @Override
  public void sendCubeProcessedEvent(int cubeCount) {
    try {
      ObjectNode root = mapper.createObjectNode();
      root.put("type", "pick_and_place");

      ObjectNode data = mapper.createObjectNode();
      data.put("number_of_pieces", cubeCount);
      data.put("order_id", 1);
      root.set("data", data);

      byte[] payload = mapper.writeValueAsString(root).getBytes(StandardCharsets.UTF_8);
      tcpMessageSender.sendAndReceive(payload);
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
      ObjectNode root = mapper.createObjectNode();
      root.put("type", "process_single_piece");

      ObjectNode data = mapper.createObjectNode();
      data.put("color", color.toUpperCase());
      data.put("action", action.name().toLowerCase());

      root.set("data", data);

      byte[] payload = mapper.writeValueAsString(root).getBytes(StandardCharsets.UTF_8);
      tcpMessageSender.sendAndReceive(payload);
    } catch (Exception e) {
      LOGGER.error("Failed to send cube manipulation event: {} {}", color, action, e);
      throw new DataProcessingException("Failed to send cube manipulation event", e);
    }
  }


  public CompletableFuture<List<PriceData>> sendAwattarDataRequest(TomorrowDataRequest.Which which) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = mapper.createObjectNode();
        data.put("which", which.name().toLowerCase());
        root.set("data", data);
        root.put("type", "request_awattar_data");

        byte[] payload = mapper.writeValueAsString(root).getBytes(StandardCharsets.UTF_8);
        byte[] response = tcpMessageSender.sendAndReceive(payload);
        return priceDataTransformer.parseTcpResponse(response);

      } catch (Exception e) {
        LOGGER.error("Failed to send awattar data request for '{}': {}", which, e.getMessage(), e);
        throw new DataProcessingException("Failed to send awattar data request", e);
      }
    });
  }

}
