package com.die_macher.application.flow;

import com.die_macher.application.service.RobotDataService;
import com.die_macher.application.transformer.RobotDataTransformer;
import com.die_macher.domain.model.robot.RobotData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

@Configuration
public class RobotDataFlow {
  private static final String ROBOT_FLOW_INPUT = "robotFlowInput";

  private final RobotDataService robotDataService;
  private final RobotDataTransformer robotDataTransformer;

  public RobotDataFlow(
      RobotDataService robotDataService, RobotDataTransformer robotDataTransformer) {
    this.robotDataService = robotDataService;
    this.robotDataTransformer = robotDataTransformer;
  }

  @Bean
  public MessageChannel robotFlowInput() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow robotFlow() {
    return IntegrationFlow.from(ROBOT_FLOW_INPUT)
        .transform(robotDataTransformer::transformRawMessage) // RobotData
        .publishSubscribeChannel(
            c ->
                c.subscribe(
                        f ->
                            f.<RobotData>handle(
                                (payload, headers) -> {
                                  robotDataService.storeRobotData(payload);
                                  return null;
                                }))
                    .subscribe(
                        f ->
                            f.<RobotData>handle(
                                (payload, headers) -> {
                                  robotDataService.publishToMqtt(payload);
                                  return null;
                                })))
        .get();
  }
}
