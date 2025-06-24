'use client';

import { useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import mqtt from 'mqtt';
import { RootState } from '@/store';
import { setClient, setConnected, setError, addSubscription } from '@/store/slices/mqtt-slice';
import { topicHandlers, getTopicHandler } from '@/services/mqtt/topic-handlers';
import { createMqttConfig } from '@/services/mqtt/config';

export const useMqtt = (brokerUrl: string) => {
  const dispatch = useDispatch();
  const { client, isConnected } = useSelector((state: RootState) => state.mqtt);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | undefined>(undefined);

  useEffect(() => {
    if (!brokerUrl) return;

    const config = createMqttConfig(brokerUrl);
    const mqttClient = mqtt.connect(config.brokerUrl, {
      clientId: config.clientId,
      clean: config.clean,
      connectTimeout: config.connectTimeout,
      reconnectPeriod: config.reconnectPeriod,
      username: config.username,
      password: config.password,
    });

    mqttClient.on('connect', () => {
      console.log('MQTT Connected');
      dispatch(setConnected(true));
      dispatch(setClient(mqttClient));

      // Subscribe to all configured topics
      topicHandlers.forEach(({ topic }) => {
        mqttClient.subscribe(topic, (err) => {
          if (err) {
            dispatch(setError(`Failed to subscribe to ${topic}`));
          } else {
            dispatch(addSubscription(topic));
            console.log(`Subscribed to ${topic}`);
          }
        });
      });
    });

    mqttClient.on('message', (topic, message) => {
      try {
        const data = JSON.parse(message.toString());
        console.log(`Received data from ${topic}:`, data);

        const handler = getTopicHandler(topic);
        if (handler) {
          handler.handler(data, dispatch);
        } else {
          console.warn(`No handler found for topic: ${topic}`);
        }
      } catch (error) {
        console.error(`Failed to parse MQTT message from ${topic}:`, error);
        dispatch(setError(`Failed to parse message from ${topic}`));
      }
    });

    mqttClient.on('error', (error) => {
      console.error('MQTT Error:', error);
      dispatch(setError(error.message));
      dispatch(setConnected(false));
    });

    mqttClient.on('close', () => {
      console.log('MQTT Connection closed');
      dispatch(setConnected(false));
    });

    mqttClient.on('offline', () => {
      console.log('MQTT Client offline');
      dispatch(setConnected(false));
    });

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      mqttClient.end();
    };
  }, [brokerUrl, dispatch]);

  const subscribe = (topic: string) => {
    if (client && isConnected) {
      client.subscribe(topic, (err) => {
        if (!err) {
          dispatch(addSubscription(topic));
        }
      });
    }
  };

  const publish = (topic: string, message: string) => {
    if (client && isConnected) {
      client.publish(topic, message);
    }
  };

  return { subscribe, publish, isConnected };
};
