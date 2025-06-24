'use client';

import React, { createContext, useContext } from 'react';
import { useMqtt } from '@/hooks/use-mqtt'; // Adjust import path if needed

interface MqttContextType {
  isConnected: boolean;
  // Add more values from useMqtt if needed, e.g. publish, subscribe, etc.
}

const MqttContext = createContext<MqttContextType | undefined>(undefined);

export const MqttProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isConnected } = useMqtt('ws://localhost:8083/mqtt');

  return <MqttContext.Provider value={{ isConnected }}>{children}</MqttContext.Provider>;
};

export const useMqttContext = () => {
  const context = useContext(MqttContext);
  if (!context) {
    throw new Error('useMqttContext must be used within a MqttProvider');
  }
  return context;
};
