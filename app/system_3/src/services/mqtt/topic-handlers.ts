import { Dispatch } from '@reduxjs/toolkit';
import { SensorData, RobotData, PriceData } from '@/types/data';
import { addRealtimeData as addSensorData } from '@/store/slices/sensor-slice';
import { addRealtimeData as addRobotData } from '@/store/slices/robot-slice';
import { addRealtimeData as addPriceData } from '@/store/slices/price-slice';

export interface TopicHandler {
  topic: string;
  handler: (data: any, dispatch: Dispatch) => void;
}

export const topicHandlers: TopicHandler[] = [
  {
    topic: 'system2/data/sensordata',
    handler: (data: SensorData, dispatch: Dispatch) => {
      dispatch(addSensorData(data));
    },
  },
  {
    topic: 'system2/data/robotdata',
    handler: (data: RobotData, dispatch: Dispatch) => {
      dispatch(addRobotData(data));
    },
  },
  {
    topic: 'system2/data/pricedata',
    handler: (data: PriceData, dispatch: Dispatch) => {
      dispatch(addPriceData(data));
    },
  },
];

export const getTopicHandler = (topic: string): TopicHandler | undefined => {
  return topicHandlers.find((handler) => handler.topic === topic);
};
