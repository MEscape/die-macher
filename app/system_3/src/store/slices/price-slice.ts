import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { PriceData } from '@/types/data';

interface PriceState {
  historicalData: PriceData[];
  aggregatedData: Partial<Record<string, PriceData>>;
  realtimeData: PriceData | null;
  loading: boolean;
  error: string | null;
  selectedTimeRange: string;
}

const initialState: PriceState = {
  historicalData: [],
  aggregatedData: {},
  realtimeData: null,
  loading: false,
  error: null,
  selectedTimeRange: '1h',
};

export const fetchHistoricalPriceData = createAsyncThunk(
  'price/fetchHistoricalData',
  async ({ start, end }: { start: string; end: string }) => {
    const response = await fetch(
      `http://localhost:8080/api/v1/price-data?start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`
    );
    if (!response.ok) throw new Error('Failed to fetch historical price data');
    return response.json();
  }
);

export const fetchAggregatedPriceData = createAsyncThunk(
  'price/fetchAggregatedData',
  async ({
    field,
    start,
    end,
    interval,
  }: {
    field: string;
    start: string;
    end: string;
    interval?: string;
  }) => {
    const response = await fetch(
      `http://localhost:8080/api/v1/price-data/aggregated?field=${field}&start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}&interval=${interval || '1h'}`
    );
    if (!response.ok) throw new Error('Failed to fetch aggregated price data');

    const data = await response.json();
    return { field, data };
  }
);

const priceSlice = createSlice({
  name: 'price',
  initialState,
  reducers: {
    addRealtimeData: (state, action: PayloadAction<PriceData>) => {
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
      .addCase(fetchHistoricalPriceData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchHistoricalPriceData.fulfilled, (state, action) => {
        state.loading = false;
        state.historicalData = action.payload;
        state.realtimeData = action.payload[action.payload.length - 1];
      })
      .addCase(fetchHistoricalPriceData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch historical price data';
      })

      // Aggregated data
      .addCase(fetchAggregatedPriceData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAggregatedPriceData.fulfilled, (state, action) => {
        state.loading = false;
        const { field, data } = action.payload;
        state.aggregatedData[field] = data;
      })
      .addCase(fetchAggregatedPriceData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch aggregated price data';
      });
  },
});

export const { addRealtimeData, setTimeRange, clearError } = priceSlice.actions;
export default priceSlice.reducer;
