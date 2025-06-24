export interface MqttConfig {
  brokerUrl: string;
  clientId: string;
  username?: string;
  password?: string;
  connectTimeout?: number;
  reconnectPeriod?: number;
  clean?: boolean;
}

export const createMqttConfig = (brokerUrl: string): MqttConfig => ({
  brokerUrl,
  clientId: `dashboard_${Math.random().toString(16).slice(2, 10)}`,
  clean: true,
  connectTimeout: 4000,
  reconnectPeriod: 1000,
  username: 'admin123',
  password: 'admin123',
});
