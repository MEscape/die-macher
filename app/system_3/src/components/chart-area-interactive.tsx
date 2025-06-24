'use client';

import * as React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from 'recharts';

import { useIsMobile } from '@/hooks/use-mobile';
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from '@/components/ui/chart';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';

import { RootState } from '@/store'; // adjust import to your store location
import { fetchHistoricalData, setTimeRange } from '@/store/slices/sensor-slice';
import { SensorData } from '@/types/data';
import { selectHistoricalSensorData } from '@/store/selector/sensor'; // adjust path

const chartConfig = {
  temperature: {
    label: ' Temperature',
    color: 'var(--primary)',
  },
  humidity: {
    label: ' Humidity',
    color: 'var(--primary)',
  },
} satisfies ChartConfig;

interface ChartDataPoint {
  timestamp: string;
  temperature?: number;
  humidity?: number;
}

export function ChartAreaInteractive() {
  const dispatch = useDispatch();
  const isMobile = useIsMobile();

  // Get historicalData and selectedTimeRange from Redux
  const historicalData: SensorData[] = useSelector((state: RootState) =>
    selectHistoricalSensorData(state)
  );
  const selectedTimeRange = useSelector((state: RootState) => state.sensors.selectedTimeRange);

  React.useEffect(() => {
    if (isMobile && selectedTimeRange !== '10min') {
      dispatch(setTimeRange('10min'));
    }
  }, [isMobile, selectedTimeRange, dispatch]);

  React.useEffect(() => {
    // Calculate start and end dates based on selectedTimeRange
    const referenceDate = new Date(); // Use current date instead of fixed date
    let minutesToSubtract = 1440; // Default to 1 day (1440 minutes)

    if (selectedTimeRange === '10min') minutesToSubtract = 10;
    else if (selectedTimeRange === '1h') minutesToSubtract = 60;
    else if (selectedTimeRange === '1d') minutesToSubtract = 1440;

    const startDate = new Date(referenceDate);
    startDate.setMinutes(startDate.getMinutes() - minutesToSubtract);

    const startStr = startDate.toISOString();
    const endStr = referenceDate.toISOString();

    // Dispatch thunk to fetch data
    dispatch(fetchHistoricalData({ start: startStr, end: endStr }));
  }, [selectedTimeRange, dispatch]);

  // Transform and group sensor data by timestamp
  const chartData = React.useMemo(() => {
    if (!historicalData || historicalData.length === 0) return [];

    // Group readings by timestamp with different granularity based on time range
    const groupedData: Record<string, ChartDataPoint> = {};

    // Determine grouping granularity based on time range
    let groupingSeconds = 60; // Default: group by minute
    if (selectedTimeRange === '10min')
      groupingSeconds = 10; // Group by 10 seconds
    else if (selectedTimeRange === '1h')
      groupingSeconds = 60; // Group by minute
    else if (selectedTimeRange === '1d') groupingSeconds = 300; // Group by 5 minutes

    historicalData.forEach((reading) => {
      // Round timestamp based on grouping granularity
      const date = new Date(reading.timestamp);
      const roundedSeconds = Math.floor(date.getSeconds() / groupingSeconds) * groupingSeconds;
      date.setSeconds(roundedSeconds, 0);
      const key = date.toISOString();

      if (!groupedData[key]) {
        groupedData[key] = {
          timestamp: key,
        };
      }

      if (reading.type === 'TEMPERATURE') {
        groupedData[key].temperature = reading.value;
      } else if (reading.type === 'HUMIDITY') {
        groupedData[key].humidity = reading.value;
      }
    });

    const sortedData = Object.values(groupedData).sort(
      (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );

    // Fill gaps with interpolated values to make chart smoother
    const filledData: ChartDataPoint[] = [];

    if (sortedData.length > 0) {
      let lastTempValue: number | undefined;
      let lastHumidityValue: number | undefined;

      sortedData.forEach((point, index) => {
        // Update last known values
        if (point.temperature !== undefined) lastTempValue = point.temperature;
        if (point.humidity !== undefined) lastHumidityValue = point.humidity;

        // Fill missing values with last known values (carry forward)
        const filledPoint: ChartDataPoint = {
          timestamp: point.timestamp,
          temperature: point.temperature ?? lastTempValue,
          humidity: point.humidity ?? lastHumidityValue,
        };

        filledData.push(filledPoint);

        // Add interpolated points for larger gaps
        if (index < sortedData.length - 1) {
          const currentTime = new Date(point.timestamp).getTime();
          const nextTime = new Date(sortedData[index + 1].timestamp).getTime();
          const timeDiff = nextTime - currentTime;
          const expectedInterval = groupingSeconds * 1000; // Convert to milliseconds

          // If gap is larger than 2x expected interval, add interpolated points
          if (timeDiff > expectedInterval * 2) {
            const stepsToFill = Math.min(Math.floor(timeDiff / expectedInterval) - 1, 5); // Limit to max 5 interpolated points

            for (let step = 1; step <= stepsToFill; step++) {
              const interpolatedTime = new Date(currentTime + step * expectedInterval);
              const progress = step / (stepsToFill + 1);

              const nextPoint = sortedData[index + 1];

              filledData.push({
                timestamp: interpolatedTime.toISOString(),
                temperature:
                  filledPoint.temperature !== undefined && nextPoint.temperature !== undefined
                    ? filledPoint.temperature +
                      (nextPoint.temperature - filledPoint.temperature) * progress
                    : filledPoint.temperature,
                humidity:
                  filledPoint.humidity !== undefined && nextPoint.humidity !== undefined
                    ? filledPoint.humidity + (nextPoint.humidity - filledPoint.humidity) * progress
                    : filledPoint.humidity,
              });
            }
          }
        }
      });
    }

    return filledData;
  }, [historicalData, selectedTimeRange]);

  // Filter data based on time range
  const filteredData = React.useMemo(() => {
    if (!chartData || chartData.length === 0) return [];

    const referenceDate = new Date();
    let minutesToSubtract = 1440; // Default to 1 day

    if (selectedTimeRange === '10min') minutesToSubtract = 10;
    else if (selectedTimeRange === '1h') minutesToSubtract = 60;
    else if (selectedTimeRange === '1d') minutesToSubtract = 1440;

    const startDate = new Date(referenceDate);
    startDate.setMinutes(startDate.getMinutes() - minutesToSubtract);

    return chartData.filter((item) => {
      const date = new Date(item.timestamp);
      return date >= startDate;
    });
  }, [chartData, selectedTimeRange]);

  // Format timestamp for X-axis based on time range
  const formatXAxisTick = (value: string) => {
    const date = new Date(value);

    if (selectedTimeRange === '10min') {
      // Show minutes and seconds for 10min range
      return date.toLocaleTimeString('en-US', {
        minute: '2-digit',
        second: '2-digit',
      });
    } else if (selectedTimeRange === '1h') {
      // Show hour and minutes for 1h range
      return date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
      });
    } else {
      // Show month, day, and hour for 1d range
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
      });
    }
  };

  // Format tooltip timestamp based on time range
  const formatTooltipTimestamp = (value: string) => {
    const date = new Date(value);

    if (selectedTimeRange === '10min') {
      return date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      });
    } else if (selectedTimeRange === '1h') {
      return date.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
      });
    } else {
      return date.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    }
  };

  return (
    <Card className='@container/card'>
      <CardHeader>
        <CardTitle>Sensor Readings</CardTitle>
        <CardDescription>
          <span className='hidden @[540px]/card:block'>
            Temperature and Humidity readings for the selected time period
          </span>
          <span className='@[540px]/card:hidden'>Real-time sensor data</span>
        </CardDescription>
        <CardAction>
          {/* ToggleGroup and Select for time range remain unchanged */}
          <ToggleGroup
            type='single'
            value={selectedTimeRange}
            onValueChange={(val) => val && dispatch(setTimeRange(val))}
            variant='outline'
            className='hidden *:data-[slot=toggle-group-item]:!px-4 @[767px]/card:flex'
          >
            <ToggleGroupItem value='1d'>Last 24 hours</ToggleGroupItem>
            <ToggleGroupItem value='1h'>Last hour</ToggleGroupItem>
            <ToggleGroupItem value='10min'>Last 10 minutes</ToggleGroupItem>
          </ToggleGroup>
          <Select
            value={selectedTimeRange}
            onValueChange={(val) => val && dispatch(setTimeRange(val))}
          >
            <SelectTrigger
              className='flex w-40 **:data-[slot=select-value]:block **:data-[slot=select-value]:truncate @[767px]/card:hidden'
              size='sm'
              aria-label='Select a value'
            >
              <SelectValue placeholder='Last 24 hours' />
            </SelectTrigger>
            <SelectContent className='rounded-xl'>
              <SelectItem value='1d' className='rounded-lg'>
                Last 24 hours
              </SelectItem>
              <SelectItem value='1h' className='rounded-lg'>
                Last hour
              </SelectItem>
              <SelectItem value='10min' className='rounded-lg'>
                Last 10 minutes
              </SelectItem>
            </SelectContent>
          </Select>
        </CardAction>
      </CardHeader>
      <CardContent className='space-y-8 px-2 pt-4 sm:px-6 sm:pt-6'>
        {/* Temperature Chart */}
        <ChartContainer
          config={{ temperature: chartConfig.temperature }}
          className='aspect-auto h-[250px] w-full'
        >
          <AreaChart data={filteredData}>
            <defs>
              <linearGradient id='fillTemperature' x1='0' y1='0' x2='0' y2='1'>
                <stop offset='5%' stopColor='var(--color-temperature)' stopOpacity={0.8} />
                <stop offset='95%' stopColor='var(--color-temperature)' stopOpacity={0.1} />
              </linearGradient>
            </defs>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey='timestamp'
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              minTickGap={32}
              tickFormatter={formatXAxisTick}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              tickFormatter={(value) => `${value}°C`}
            />
            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  labelFormatter={formatTooltipTimestamp}
                  indicator='dot'
                  formatter={(value) => [`${value}°C`, ' Temperature']}
                />
              }
            />
            <Area
              dataKey='temperature'
              type='monotone'
              fill='url(#fillTemperature)'
              stroke='var(--color-temperature)'
              strokeWidth={2}
              dot={false}
              connectNulls={true}
            />
          </AreaChart>
        </ChartContainer>

        {/* Humidity Chart */}
        <ChartContainer
          config={{ humidity: chartConfig.humidity }}
          className='aspect-auto h-[250px] w-full'
        >
          <AreaChart data={filteredData}>
            <defs>
              <linearGradient id='fillHumidity' x1='0' y1='0' x2='0' y2='1'>
                <stop offset='5%' stopColor='var(--color-humidity)' stopOpacity={0.8} />
                <stop offset='95%' stopColor='var(--color-humidity)' stopOpacity={0.1} />
              </linearGradient>
            </defs>
            <CartesianGrid vertical={false} />
            <XAxis
              dataKey='timestamp'
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              minTickGap={32}
              tickFormatter={formatXAxisTick}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tickMargin={8}
              tickFormatter={(value) => `${value}%`}
            />
            <ChartTooltip
              cursor={false}
              content={
                <ChartTooltipContent
                  labelFormatter={formatTooltipTimestamp}
                  indicator='dot'
                  formatter={(value) => [`${value}%`, ' Humidity']}
                />
              }
            />
            <Area
              dataKey='humidity'
              type='monotone'
              fill='url(#fillHumidity)'
              stroke='var(--color-humidity)'
              strokeWidth={2}
              dot={false}
              connectNulls={true}
            />
          </AreaChart>
        </ChartContainer>

        {/* Show message if no data */}
        {(!filteredData || filteredData.length === 0) && (
          <div className='text-muted-foreground flex h-[200px] items-center justify-center'>
            No sensor data available for the selected time range
          </div>
        )}
      </CardContent>
    </Card>
  );
}
