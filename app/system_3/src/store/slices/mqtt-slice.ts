import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { MqttClient } from 'mqtt';

interface MqttState {
  client: MqttClient | null;
  isConnected: boolean;
  error: string | null;
  subscriptions: string[];
}

const initialState: MqttState = {
  client: null,
  isConnected: false,
  error: null,
  subscriptions: [],
};

const mqttSlice = createSlice({
  name: 'mqtt',
  initialState,
  reducers: {
    setClient: (state, action: PayloadAction<MqttClient | null>) => {
      state.client = action.payload;
    },
    setConnected: (state, action: PayloadAction<boolean>) => {
      state.isConnected = action.payload;
      if (action.payload) {
        state.error = null;
      }
    },
    setError: (state, action: PayloadAction<string>) => {
      state.error = action.payload;
    },
    addSubscription: (state, action: PayloadAction<string>) => {
      if (!state.subscriptions.includes(action.payload)) {
        state.subscriptions.push(action.payload);
      }
    },
    removeSubscription: (state, action: PayloadAction<string>) => {
      state.subscriptions = state.subscriptions.filter((sub) => sub !== action.payload);
    },
  },
});

export const { setClient, setConnected, setError, addSubscription, removeSubscription } =
  mqttSlice.actions;
export default mqttSlice.reducer;
