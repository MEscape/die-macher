import { RootState } from '@/store';

export const selectRealtimePriceData = (state: RootState) => state.price.realtimeData;

export const selectAggregatedPriceData = (state: RootState) => state.price.aggregatedData;
