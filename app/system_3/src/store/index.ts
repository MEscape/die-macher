import { configureStore } from '@reduxjs/toolkit';
import mqttReducer from './slices/mqtt-slice';
import sensorReducer from './slices/sensor-slice';
import robotReducer from './slices/robot-slice';
import priceReducer from './slices/price-slice';

export const store = configureStore({
  reducer: {
    mqtt: mqttReducer,
    sensors: sensorReducer,
    robot: robotReducer,
    price: priceReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['mqtt/setClient'],
        ignoredPaths: ['mqtt.client'],
      },
    }),
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
