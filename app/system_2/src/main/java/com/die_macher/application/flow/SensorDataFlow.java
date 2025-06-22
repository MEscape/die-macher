package com.die_macher.application.flow;

import com.die_macher.application.service.SensorDataService;
import com.die_macher.application.transformer.SensorDataTransformer;
import com.die_macher.domain.model.SensorData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

@Configuration
public class SensorDataFlow {
  private static final String SENSOR_FLOW_INPUT = "sensorFlowInput";

  private final SensorDataService sensorDataService;
  private final SensorDataTransformer sensorDataTransformer;

  public SensorDataFlow(
      SensorDataService sensorDataService, SensorDataTransformer sensorDataTransformer) {
    this.sensorDataService = sensorDataService;
    this.sensorDataTransformer = sensorDataTransformer;
  }

  @Bean
  public MessageChannel sensorFlowInput() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow sensorFlow() {
    return IntegrationFlow.from(SENSOR_FLOW_INPUT)
        .transform(sensorDataTransformer::transformRawMessage) // List<SensorData>
        .split() // emits SensorData one-by-one
        .publishSubscribeChannel(
            c ->
                c.subscribe(
                        f ->
                            f.<SensorData>handle(
                                (payload, headers) -> {
                                  sensorDataService.storeSensorData(payload);
                                  return null;
                                }))
                    .subscribe(
                        f ->
                            f.<SensorData>handle(
                                (payload, headers) -> {
                                  sensorDataService.publishToMqtt(payload);
                                  return null;
                                })))
        .get();
  }
}
