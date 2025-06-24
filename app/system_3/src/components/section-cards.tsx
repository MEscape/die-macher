'use client';

import { IconMathAvg } from '@tabler/icons-react';

import { Badge } from '@/components/ui/badge';
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { selectAggregatedSensorData, selectRealtimeSensorData } from '@/store/selector/sensor';
import { RootState } from '@/store';
import { useSelector, useDispatch } from 'react-redux';
import { PriceData, SensorData, SensorType } from '@/types/data';
import { selectAggregatedPriceData, selectRealtimePriceData } from '@/store/selector/price';
import { fetchAggregatedSensorData } from '@/store/slices/sensor-slice';
import { useEffect } from 'react';
import { fetchAggregatedPriceData } from '@/store/slices/price-slice';

const AGGREGATION_RANGE = 24;

export function SectionCards() {
  const dispatch = useDispatch();

  const realtimeTemperature: SensorData = useSelector((state: RootState) =>
    selectRealtimeSensorData(state, 'TEMPERATURE')
  );

  const realtimeHumidity: SensorData = useSelector((state: RootState) =>
    selectRealtimeSensorData(state, 'HUMIDITY')
  );

  const realtimePrice: PriceData = useSelector((state: RootState) =>
    selectRealtimePriceData(state)
  );

  const aggregatedSensor: Partial<Record<SensorType, SensorData>> = useSelector(
    (state: RootState) => selectAggregatedSensorData(state)
  );

  const aggregatedPrice: Partial<Record<string, PriceData>> = useSelector((state: RootState) =>
    selectAggregatedPriceData(state)
  );

  // Fetch aggregated data for trends (last 24 hours vs previous 24 hours)
  useEffect(() => {
    const now = new Date();
    const end = now.toISOString();
    const start = new Date(now.getTime() - AGGREGATION_RANGE * 60 * 60 * 1000).toISOString(); // Last 24 hours

    // Fetch aggregated data for temperature
    dispatch(
      fetchAggregatedSensorData({
        sensorType: 'TEMPERATURE',
        start,
        end,
      })
    );

    // Fetch aggregated data for humidity
    dispatch(
      fetchAggregatedSensorData({
        sensorType: 'HUMIDITY',
        start,
        end,
      })
    );

    dispatch(
      fetchAggregatedPriceData({
        field: 'total_cost',
        start,
        end,
      })
    );

    dispatch(
      fetchAggregatedPriceData({
        field: 'price_per_kwh',
        start,
        end,
      })
    );
  }, [dispatch]);

  return (
    <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid grid-cols-1 gap-4 px-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4'>
      <Card className='@container/card'>
        <CardHeader>
          <CardDescription>Current Temperature</CardDescription>
          <CardTitle className='text-2xl font-semibold tabular-nums @[250px]/card:text-3xl'>
            {realtimeTemperature?.value && realtimeTemperature?.unit
              ? `${realtimeTemperature.value} ${realtimeTemperature.unit}`
              : '---'}
          </CardTitle>
          <CardAction>
            <Badge variant='outline'>
              <IconMathAvg />
              {aggregatedSensor.TEMPERATURE?.value && realtimeTemperature?.unit
                ? `${aggregatedSensor.TEMPERATURE.value.toFixed(2)} ${realtimeTemperature.unit}`
                : '-'}
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className='flex-col items-start gap-1.5 text-sm'>
          <div className='text-muted-foreground'>{`Average Temperature for the last ${AGGREGATION_RANGE}h`}</div>
        </CardFooter>
      </Card>
      <Card className='@container/card'>
        <CardHeader>
          <CardDescription>Current Humidity</CardDescription>
          <CardTitle className='text-2xl font-semibold tabular-nums @[250px]/card:text-3xl'>
            {realtimeHumidity?.value != null && realtimeHumidity?.unit
              ? `${realtimeHumidity.value} ${realtimeHumidity.unit}`
              : '---'}
          </CardTitle>
          <CardAction>
            <Badge variant='outline'>
              <IconMathAvg />
              {aggregatedSensor.HUMIDITY?.value && realtimeHumidity?.unit
                ? `${aggregatedSensor.HUMIDITY.value.toFixed(2)} ${realtimeHumidity.unit}`
                : '-'}
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className='flex-col items-start gap-1.5 text-sm'>
          <div className='text-muted-foreground'>{`Average Humidity for the last ${AGGREGATION_RANGE}h`}</div>
        </CardFooter>
      </Card>
      <Card className='@container/card'>
        <CardHeader>
          <CardDescription>Current Electricity Price</CardDescription>
          <CardTitle className='text-2xl font-semibold tabular-nums @[250px]/card:text-3xl'>
            {realtimePrice?.priceInEurPerKwh != null
              ? `${realtimePrice.priceInEurPerKwh.toFixed(4)} €/kWh`
              : '---'}
          </CardTitle>
          <CardAction>
            <Badge variant='outline'>
              <IconMathAvg />
              {aggregatedPrice.price_per_kwh?.priceInEurPerKwh
                ? `${aggregatedPrice.price_per_kwh.priceInEurPerKwh.toFixed(2)} €/kWh`
                : '-'}
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className='flex-col items-start gap-1.5 text-sm'>
          <div className='text-muted-foreground'>{`Average Electricity Price for the last ${AGGREGATION_RANGE}h`}</div>
        </CardFooter>
      </Card>
      <Card className='@container/card'>
        <CardHeader>
          <CardDescription>Total Electricity Price</CardDescription>
          <CardTitle className='text-2xl font-semibold tabular-nums @[250px]/card:text-3xl'>
            {realtimePrice?.totalCost != null ? `${realtimePrice.totalCost.toFixed(2)} €` : '---'}
          </CardTitle>
          <CardAction>
            <Badge variant='outline'>
              <IconMathAvg />
              {aggregatedPrice.total_cost?.totalCost
                ? `${aggregatedPrice.total_cost.totalCost.toFixed(2)} €`
                : '-'}
            </Badge>
          </CardAction>
        </CardHeader>
        <CardFooter className='flex-col items-start gap-1.5 text-sm'>
          <div className='text-muted-foreground'>{`Average Total Price for the last ${AGGREGATION_RANGE}h`}</div>
        </CardFooter>
      </Card>
    </div>
  );
}
