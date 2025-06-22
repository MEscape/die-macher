package com.die_macher.infrastructure.adapter.messaging;

import com.die_macher.infrastructure.config.properties.MqttProperties;
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
    public CompletableFuture<Void> publish(Object payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                String jsonPayload = objectMapper.writeValueAsString(payload);
                String topic = buildDynamicTopicName(payload);

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
                LOGGER.error("Failed to publish MQTT message for payload: {}", payload, e);
                throw new RuntimeException("MQTT publish failed", e);
            }
        });
    }

    private String buildDynamicTopicName(Object payload) {
        String baseTopic = mqttProperties.getBroker().getTopic();
        String typeSegment = payload.getClass().getSimpleName().toLowerCase();

        return String.format("%s/%s", baseTopic, typeSegment);
    }
}
