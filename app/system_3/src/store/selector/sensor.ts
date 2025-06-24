import { RootState } from '@/store';
import { SensorType } from '@/types/data';

export const selectRealtimeSensorData = (state: RootState, type: SensorType) =>
  state.sensors.realtimeData[type];

export const selectAggregatedSensorData = (state: RootState) => state.sensors.aggregatedData;

export const selectHistoricalSensorData = (state: RootState) => state.sensors.historicalData;
