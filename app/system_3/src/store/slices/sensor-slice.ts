import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { SensorData, SensorType } from '@/types/data';

interface SensorState {
  historicalData: SensorData[];
  realtimeData: Partial<Record<SensorType, SensorData>>;
  aggregatedData: Partial<Record<SensorType, SensorData>>;
  loading: boolean;
  error: string | null;
  selectedTimeRange: string;
}

const initialState: SensorState = {
  historicalData: [],
  realtimeData: {},
  aggregatedData: {},
  loading: false,
  error: null,
  selectedTimeRange: '1h',
};

const SENSOR_ID = 'sensor_000';

export const fetchHistoricalData = createAsyncThunk(
  'sensors/fetchHistoricalData',
  async ({ start, end }: { start: string; end: string }) => {
    const response = await fetch(
      `http://localhost:8080/api/v1/sensor-data?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`
    );
    if (!response.ok) throw new Error('Failed to fetch historical data');
    return response.json();
  }
);

export const fetchAggregatedSensorData = createAsyncThunk(
  'sensors/fetchAggregatedSensorData',
  async ({
    sensorType,
    start,
    end,
    interval = '1h',
  }: {
    sensorId: string;
    sensorType: SensorType;
    start: string;
    end: string;
    interval?: string;
  }) => {
    const params = new URLSearchParams({
      sensorId: SENSOR_ID,
      sensorType,
      start,
      end,
      interval,
    });

    const response = await fetch(
      `http://localhost:8080/api/v1/sensor-data/aggregated?${params.toString()}`
    );

    if (!response.ok) {
      throw new Error('Failed to fetch aggregated sensor data');
    }

    const data = await response.json();
    return { sensorType: sensorType as SensorType, data };
  }
);

const sensorSlice = createSlice({
  name: 'sensors',
  initialState,
  reducers: {
    addRealtimeData: (state, action: PayloadAction<SensorData>) => {
      const data = action.payload;

      // Update realtimeData if type is known
      state.realtimeData[data.type as SensorType] = data;

      // Update historicalData
      state.historicalData.push(data);
      if (state.historicalData.length > 100) {
        state.historicalData.shift();
      }
    },
    setTimeRange: (state, action: PayloadAction<string>) => {
      state.selectedTimeRange = action.payload;
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Historical data
      .addCase(fetchHistoricalData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchHistoricalData.fulfilled, (state, action) => {
        state.loading = false;
        state.historicalData = action.payload;
        state.realtimeData = action.payload[action.payload.length - 1];
      })
      .addCase(fetchHistoricalData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch historical data';
      })

      // Aggregated data
      .addCase(fetchAggregatedSensorData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAggregatedSensorData.fulfilled, (state, action) => {
        state.loading = false;
        const { sensorType, data } = action.payload;
        state.aggregatedData[sensorType] = data;
      })
      .addCase(fetchAggregatedSensorData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch aggregated sensor data';
      });
  },
});

export const { addRealtimeData, setTimeRange, clearError } = sensorSlice.actions;
export default sensorSlice.reducer;
