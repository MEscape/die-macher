export interface BaseData {
  timestamp: string;
  metadata?: Record<string, any>;
}

export interface SensorData extends BaseData {
  sensorId: string;
  type: SensorType;
  value: number;
  unit: string;
}

export type SensorType = 'HUMIDITY' | 'TEMPERATURE';

export interface RobotData extends BaseData {
  robotTask: string;
  robotStatus: string;
  color: string;
}

export interface PriceData extends BaseData {
  startTimestamp: string;
  endTimestamp: string;
  totalCost: number;
  priceInEurPerKwh: number;
  startTimeFormatted: string;
  endTimeFormatted: string;
}
