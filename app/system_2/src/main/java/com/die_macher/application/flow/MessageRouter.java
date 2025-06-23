package com.die_macher.application.flow;

import com.die_macher.infrastructure.adapter.serializer.CustomHeaderDeserializer;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MessageRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouter.class);

  private static final String TCP_INPUT_CHANNEL = "tcpInputChannel";
  private static final String SENSOR_FLOW_INPUT = "sensorFlowInput";
  private static final String UNMAPPED_FLOW_CHANNEL = "unmappedFlowChannel";
  private static final String PRICE_FLOW_INPUT = "priceFlowInput";
  private static final String ROBOT_FLOW_INPUT = "robotFlowInput";

  private final CustomHeaderDeserializer customHeaderDeserializer;

  public MessageRouter(CustomHeaderDeserializer customHeaderDeserializer) {
    this.customHeaderDeserializer = customHeaderDeserializer;
  }

  @Bean
  public IntegrationFlow routerFlow() {
    return IntegrationFlow.from(TCP_INPUT_CHANNEL)
        .transform(byte[].class, payload -> new String(payload, StandardCharsets.UTF_8))
        .route(
            Message.class,
            this::resolveFlowType,
            mapping ->
                mapping
                    .channelMapping('S', SENSOR_FLOW_INPUT)
                    .channelMapping('P', PRICE_FLOW_INPUT)
                    .channelMapping('R', ROBOT_FLOW_INPUT)
                    .defaultOutputChannel(UNMAPPED_FLOW_CHANNEL))
        .get();
  }

  private char resolveFlowType(Message<?> message) {
    byte flowType = customHeaderDeserializer.getCurrentFlowType();
    char resolved = (char) flowType;
    LOGGER.info(
        "Resolved flowType: [{}] (byte: 0x{}) for message: {}",
        resolved,
        Integer.toHexString(flowType),
        message);
    customHeaderDeserializer.clearCurrentFlowType();
    return resolved;
  }

  @Bean
  public MessageChannel unmappedFlowChannel() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow unmappedFlowHandler() {
    return IntegrationFlow.from(UNMAPPED_FLOW_CHANNEL)
        .handle(message -> LOGGER.error("Received unmapped flow: {}", message))
        .get();
  }
}
