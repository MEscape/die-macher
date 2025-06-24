import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { RobotData } from '@/types/data';

interface RobotState {
  historicalData: RobotData[];
  realtimeData: RobotData | null;
  loading: boolean;
  error: string | null;
  selectedTimeRange: string;
}

const initialState: RobotState = {
  historicalData: [],
  realtimeData: null,
  loading: false,
  error: null,
  selectedTimeRange: '1h',
};

export const fetchHistoricalRobotData = createAsyncThunk(
  'robot/fetchHistoricalData',
  async ({ start, end }: { start: string; end: string }) => {
    const response = await fetch(
      `http://localhost:8080/api/v1/robot-data?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`
    );
    if (!response.ok) throw new Error('Failed to fetch historical robot data');
    return response.json();
  }
);

export const sendStartProcessingCubes = createAsyncThunk(
  'robot/sendStartProcessingCubes',
  async ({ cubeCount }: { cubeCount: number }) => {
    const response = await fetch(
      `http://localhost:8080/api/v1/robot-data/cube?cubeCount=${encodeURIComponent(cubeCount)}`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
    if (!response.ok) throw new Error('Failed to send cube manipulation');
    return null;
  }
);

export const sendCubeManipulation = createAsyncThunk(
  'robot/sendCubeManipulation',
  async ({ color, action }: { color: string; action: string }) => {
    const response = await fetch('http://localhost:8080/api/v1/robot-data/cube', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ color, action }),
    });
    if (!response.ok) throw new Error('Failed to send cube manipulation');
    return null;
  }
);

const robotSlice = createSlice({
  name: 'robot',
  initialState,
  reducers: {
    addRealtimeData: (state, action: PayloadAction<RobotData>) => {
      const data = action.payload;

      state.realtimeData = data;

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
      .addCase(fetchHistoricalRobotData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchHistoricalRobotData.fulfilled, (state, action) => {
        state.loading = false;
        state.historicalData = action.payload;
        state.realtimeData = action.payload[action.payload.length - 1];
      })
      .addCase(fetchHistoricalRobotData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch historical robot data';
      })

      // Start cube processing
      .addCase(sendStartProcessingCubes.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(sendStartProcessingCubes.fulfilled, (state, _action) => {
        state.loading = false;
      })
      .addCase(sendStartProcessingCubes.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to send cube processing command';
      })

      // Cube manipulation
      .addCase(sendCubeManipulation.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(sendCubeManipulation.fulfilled, (state, _action) => {
        state.loading = false;
      })
      .addCase(sendCubeManipulation.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to send cube manipulation';
      });
  },
});

export const { addRealtimeData, setTimeRange, clearError } = robotSlice.actions;
export default robotSlice.reducer;
