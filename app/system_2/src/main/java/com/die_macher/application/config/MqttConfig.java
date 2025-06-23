package com.die_macher.application.config;

import com.die_macher.infrastructure.config.properties.MqttProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttConfig {

  private final MqttProperties mqttProperties;

  public MqttConfig(MqttProperties mqttProperties) {
    this.mqttProperties = mqttProperties;
  }

  @Bean
  public MqttPahoClientFactory mqttClientFactory() {
    DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
    MqttConnectOptions options = new MqttConnectOptions();

    options.setServerURIs(new String[] {mqttProperties.getBroker().getUrl()});
    options.setUserName(mqttProperties.getBroker().getUsername());
    options.setPassword(mqttProperties.getBroker().getPassword().toCharArray());
    options.setCleanSession(true);
    options.setConnectionTimeout(30);
    options.setKeepAliveInterval(60);
    options.setAutomaticReconnect(true);

    factory.setConnectionOptions(options);
    return factory;
  }

  @Bean
  public MessageChannel mqttOutboundChannel() {
    return new DirectChannel();
  }

  @Bean
  @ServiceActivator(inputChannel = "mqttOutboundChannel")
  public MessageHandler mqttOutbound() {
    MqttPahoMessageHandler messageHandler =
        new MqttPahoMessageHandler(mqttProperties.getBroker().getClientId(), mqttClientFactory());

    messageHandler.setAsync(true);
    messageHandler.setDefaultTopic(mqttProperties.getBroker().getTopic());
    messageHandler.setDefaultQos(mqttProperties.getBroker().getQos());

    return messageHandler;
  }
}
