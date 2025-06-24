import { RootState } from '@/store';

export const selectHistoricalRobotData = (state: RootState) => state.robot.historicalData;
