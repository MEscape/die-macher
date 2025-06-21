package com.die_macher.infrastructure.adapter.messaging;

import com.die_macher.infrastructure.config.properties.MqttProperties;
import com.die_macher.domain.model.SensorData;
import com.die_macher.domain.port.outbound.MessagePublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
public class MqttMessagePublisher implements MessagePublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttMessagePublisher.class);
    private final MessageChannel mqttOutboundChannel;
    private final ObjectMapper objectMapper;
    private final MqttProperties mqttProperties;

    public MqttMessagePublisher(MessageChannel mqttOutboundChannel,
                                ObjectMapper objectMapper,
                                MqttProperties mqttProperties) {
        this.mqttOutboundChannel = mqttOutboundChannel;
        this.objectMapper = objectMapper;
        this.mqttProperties = mqttProperties;
    }

    @Override
    public CompletableFuture<Void> publish(SensorData sensorData) {
        return CompletableFuture.runAsync(() -> {
            try {
                String jsonPayload = objectMapper.writeValueAsString(sensorData);
                String topic = buildTopicName(sensorData);

                var message = MessageBuilder.withPayload(jsonPayload)
                        .setHeader("mqtt_topic", topic)
                        .setHeader("mqtt_qos", mqttProperties.getBroker().getQos())
                        .setHeader("mqtt_retained", false)
                        .build();

                boolean sent = mqttOutboundChannel.send(message, 5000);
                if (!sent) {
                    throw new RuntimeException("Failed to send MQTT message within timeout");
                }

                LOGGER.debug("Published sensor data to MQTT topic: {}", topic);

            } catch (Exception e) {
                LOGGER.error("Failed to publish MQTT message for sensor: {}", sensorData.sensorId(), e);
                throw new RuntimeException("MQTT publish failed", e);
            }
        });
    }

    private String buildTopicName(SensorData sensorData) {
        return String.format("%s/%s/%s",
                mqttProperties.getBroker().getTopic(),
                sensorData.type().name().toLowerCase(),
                sensorData.sensorId());
    }
}
