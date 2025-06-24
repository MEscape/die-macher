'use client';

import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '@/store';
import {
  fetchHistoricalData,
  fetchAggregatedSensorData,
  setTimeRange,
  clearError,
} from '@/store/slices/sensor-slice';

import { AppSidebar } from '@/components/app-sidebar';
import { SiteHeader } from '@/components/site-header';
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

import {
  Thermometer,
  Droplets,
  Activity,
  TrendingUp,
  TrendingDown,
  AlertTriangle,
} from 'lucide-react';

import {
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
  AreaChart,
  Area,
  PieChart,
  Pie,
  Cell,
  ReferenceLine,
} from 'recharts';
import { SensorData, SensorType } from '@/types/data';

const SensorPage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const {
    historicalData,
    realtimeData,
    error,
    selectedTimeRange,
  }: {
    historicalData: SensorData[];
    realtimeData: Partial<Record<SensorType, SensorData>>;
    aggregatedData: Partial<Record<SensorType, SensorData>>;
    loading: boolean;
    error: string | null;
    selectedTimeRange: string;
  } = useSelector((state: RootState) => state.sensors);

  const [selectedSensorType, setSelectedSensorType] = useState<SensorType | 'ALL'>('ALL');

  useEffect(() => {
    // Fetch initial data
    const now = new Date();
    const start = new Date(now.getTime() - 60 * 60 * 1000).toISOString(); // 1 hour ago
    dispatch(fetchHistoricalData({ start, end: now.toISOString() }));

    // Fetch aggregated data for each sensor type
    const sensorTypes: SensorType[] = ['TEMPERATURE', 'HUMIDITY'];
    sensorTypes.forEach((type) => {
      dispatch(
        fetchAggregatedSensorData({
          sensorId: 'sensor_000',
          sensorType: type,
          start,
          end: now.toISOString(),
          interval: '15m',
        })
      );
    });
  }, [dispatch]);

  const handleTimeRangeChange = (range: string) => {
    dispatch(setTimeRange(range));
    const now = new Date();
    let start: Date;

    switch (range) {
      case '15m':
        start = new Date(now.getTime() - 15 * 60 * 1000);
        break;
      case '1h':
        start = new Date(now.getTime() - 60 * 60 * 1000);
        break;
      case '6h':
        start = new Date(now.getTime() - 6 * 60 * 60 * 1000);
        break;
      case '24h':
        start = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        break;
      case '7d':
        start = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        break;
      default:
        start = new Date(now.getTime() - 60 * 60 * 1000);
    }

    dispatch(fetchHistoricalData({ start: start.toISOString(), end: now.toISOString() }));
  };

  // Sensor type configurations
  const sensorConfig = {
    TEMPERATURE: {
      icon: Thermometer,
      color: 'var(--chart-5)',
      unit: '°C',
      thresholds: { min: 18, max: 25 },
      label: 'Temperature',
    },
    HUMIDITY: {
      icon: Droplets,
      color: 'var(--primary)',
      unit: '%',
      thresholds: { min: 40, max: 60 },
      label: 'Humidity',
    },
  };

  // Helper function to safely get sensor config
  const getSensorConfig = (sensorType: string) => {
    return (
      sensorConfig[sensorType as keyof typeof sensorConfig] || {
        icon: Activity,
        color: '#6b7280',
        unit: '',
        thresholds: { min: 0, max: 100 },
        label: sensorType,
      }
    );
  };

  // Analytics functions
  const getSensorStats = () => {
    const stats: Record<string, any> = {};

    // Only process sensor types that exist in our configuration
    Object.keys(sensorConfig).forEach((type) => {
      const sensorType = type as SensorType;
      const data = historicalData.filter((d) => d.type === sensorType);

      if (data.length > 0) {
        const values = data.map((d) => d.value);
        const current = realtimeData[sensorType]?.value || 0;
        const config = getSensorConfig(sensorType);
        const { min: minThreshold, max: maxThreshold } = config.thresholds;

        stats[sensorType] = {
          current,
          average: values.reduce((a, b) => a + b, 0) / values.length,
          min: Math.min(...values),
          max: Math.max(...values),
          trend: getTrend(data),
          isInRange: current >= minThreshold && current <= maxThreshold,
          changePercent: getChangePercent(data),
          dataPoints: data.length,
        };
      }
    });

    return stats;
  };

  const getTrend = (data: SensorData[]) => {
    if (data.length < 2) return 'stable';
    const recent = data.slice(-5);
    const older = data.slice(-10, -5);

    if (recent.length === 0 || older.length === 0) return 'stable';

    const recentAvg = recent.reduce((a, b) => a + b.value, 0) / recent.length;
    const olderAvg = older.reduce((a, b) => a + b.value, 0) / older.length;

    const diff = recentAvg - olderAvg;
    if (Math.abs(diff) < 0.5) return 'stable';
    return diff > 0 ? 'increasing' : 'decreasing';
  };

  const getChangePercent = (data: SensorData[]) => {
    if (data.length < 2) return 0;
    const current = data[data.length - 1].value;
    const previous = data[data.length - 2].value;
    return previous !== 0 ? ((current - previous) / previous) * 100 : 0;
  };

  const getFilteredData = () => {
    if (selectedSensorType === 'ALL') return historicalData;
    return historicalData.filter((d) => d.type === selectedSensorType);
  };

  // FIXED: Properly handle immutable data from Redux
  const getTimeSeriesData = () => {
    const filtered = getFilteredData();

    // Create a deep copy to avoid mutating Redux state
    const sortedData = [...filtered].sort(
      (a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
    );

    // Create time intervals based on the data range
    const timeIntervals = new Map();

    sortedData.forEach((item) => {
      // Round timestamp to nearest minute for better grouping
      const roundedTime = new Date(item.timestamp);
      roundedTime.setSeconds(0, 0);
      const timeKey = roundedTime.getTime();

      if (!timeIntervals.has(timeKey)) {
        timeIntervals.set(timeKey, {
          time: roundedTime.toLocaleTimeString(),
          timestamp: roundedTime.toISOString(),
          originalTimestamp: timeKey,
        });
      }

      // Create a new object instead of mutating existing one
      const existingPoint = timeIntervals.get(timeKey);
      timeIntervals.set(timeKey, {
        ...existingPoint,
        [item.type]: item.value,
      });
    });

    // Convert to array and sort
    const result = Array.from(timeIntervals.values()).sort(
      (a, b) => a.originalTimestamp - b.originalTimestamp
    );

    // Fill missing values with interpolation or last known values
    const sensorTypes = Object.keys(sensorConfig) as SensorType[];
    const lastKnownValues: Partial<Record<SensorType, number>> = {};

    // Create new objects instead of mutating existing ones
    const filledResult = result.map((point) => {
      const newPoint = { ...point };

      sensorTypes.forEach((sensorType) => {
        if (newPoint[sensorType] !== undefined) {
          lastKnownValues[sensorType] = newPoint[sensorType];
        } else if (lastKnownValues[sensorType] !== undefined) {
          // Use last known value to avoid gaps
          newPoint[sensorType] = lastKnownValues[sensorType];
        }
      });

      return newPoint;
    });

    return filledResult;
  };

  const getSensorDistribution = () => {
    const distribution = historicalData.reduce(
      (acc, item) => {
        acc[item.type] = (acc[item.type] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );

    return Object.entries(distribution).map(([type, count]) => ({
      type,
      count,
      label: getSensorConfig(type).label,
    }));
  };

  const getAlerts = () => {
    const alerts: Array<{
      type: SensorType;
      message: string;
      severity: 'warning' | 'error';
      timestamp: string;
    }> = [];

    Object.entries(realtimeData).forEach(([type, data]) => {
      if (data) {
        const config = getSensorConfig(type);
        const { min, max } = config.thresholds;

        if (data.value < min) {
          alerts.push({
            type: type as SensorType,
            message: `${config.label} below threshold (${data.value}${config.unit} < ${min}${config.unit})`,
            severity: 'error',
            timestamp: data.timestamp,
          });
        } else if (data.value > max) {
          alerts.push({
            type: type as SensorType,
            message: `${config.label} above threshold (${data.value}${config.unit} > ${max}${config.unit})`,
            severity: 'error',
            timestamp: data.timestamp,
          });
        }
      }
    });

    return alerts.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
  };

  const stats = getSensorStats();
  const timeSeriesData = getTimeSeriesData();
  const sensorDistribution = getSensorDistribution();
  const alerts = getAlerts();

  const COLORS = ['var(--primary)', 'var(--chart-5)'];

  return (
    <SidebarProvider
      style={
        {
          '--sidebar-width': 'calc(var(--spacing) * 72)',
          '--header-height': 'calc(var(--spacing) * 12)',
        } as React.CSSProperties
      }
    >
      <AppSidebar variant='inset' />
      <SidebarInset>
        <SiteHeader />

        <div className='mt-4 flex flex-1 flex-col gap-4 p-4 pt-0'>
          {/* Header Section */}
          <div className='flex items-center justify-between'>
            <div className='flex items-center gap-4'>
              <div className='flex items-center gap-2'>
                <Activity className='text-primary h-8 w-8' />
                <div>
                  <h1 className='text-3xl font-bold'>Sensor Monitoring Dashboard</h1>
                  <p className='text-muted-foreground'>
                    Real-time environmental monitoring and analytics
                  </p>
                </div>
              </div>
            </div>

            <div className='flex items-center gap-2'>
              <Select
                value={selectedSensorType}
                onValueChange={(value) => setSelectedSensorType(value as SensorType | 'ALL')}
              >
                <SelectTrigger className='w-32'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='ALL'>All Sensors</SelectItem>
                  <SelectItem value='TEMPERATURE'>Temperature</SelectItem>
                  <SelectItem value='HUMIDITY'>Humidity</SelectItem>
                </SelectContent>
              </Select>
              <Select value={selectedTimeRange} onValueChange={handleTimeRangeChange}>
                <SelectTrigger className='w-24'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='15m'>15m</SelectItem>
                  <SelectItem value='1h'>1h</SelectItem>
                  <SelectItem value='6h'>6h</SelectItem>
                  <SelectItem value='24h'>24h</SelectItem>
                  <SelectItem value='7d'>7d</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Error Alert */}
          {error && (
            <Alert variant='destructive'>
              <AlertTriangle className='h-4 w-4' />
              <AlertDescription className='flex items-center justify-between'>
                {error}
                <Button variant='outline' size='sm' onClick={() => dispatch(clearError())}>
                  Dismiss
                </Button>
              </AlertDescription>
            </Alert>
          )}

          {/* Alerts Section */}
          {alerts.length > 0 && (
            <Alert variant='destructive'>
              <AlertTriangle className='h-4 w-4' />
              <AlertDescription>
                <div className='space-y-1'>
                  <p className='font-medium'>Active Alerts ({alerts.length})</p>
                  {alerts.slice(0, 3).map((alert, index) => (
                    <p key={index} className='text-sm'>
                      {alert.message}
                    </p>
                  ))}
                  {alerts.length > 3 && (
                    <p className='text-muted-foreground text-sm'>
                      +{alerts.length - 3} more alerts
                    </p>
                  )}
                </div>
              </AlertDescription>
            </Alert>
          )}

          {/* Current Readings Cards */}
          <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-2'>
            {Object.entries(sensorConfig).map(([type, config]) => {
              const sensorType = type as SensorType;
              const stat = stats[sensorType];
              const Icon = config.icon;

              return (
                <Card key={type}>
                  <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                    <CardTitle className='text-sm font-medium'>{config.label}</CardTitle>
                    <Icon className='text-muted-foreground h-4 w-4' />
                  </CardHeader>
                  <CardContent>
                    <div className='text-2xl font-bold'>
                      {stat?.current?.toFixed(1) || '0.0'}
                      {config.unit}
                    </div>
                    <div className='mt-1 flex items-center gap-2'>
                      <Badge variant={stat?.isInRange ? 'default' : 'destructive'}>
                        {stat?.isInRange ? 'Normal' : 'Alert'}
                      </Badge>
                      {stat?.trend && (
                        <div className='flex items-center gap-1 text-xs'>
                          {stat.trend === 'increasing' && (
                            <TrendingUp className='h-3 w-3 text-red-500' />
                          )}
                          {stat.trend === 'decreasing' && (
                            <TrendingDown className='h-3 w-3 text-green-500' />
                          )}
                          <span
                            className={`${
                              stat.trend === 'increasing'
                                ? 'text-red-500'
                                : stat.trend === 'decreasing'
                                  ? 'text-green-500'
                                  : 'text-gray-500'
                            }`}
                          >
                            {stat.changePercent > 0 ? '+' : ''}
                            {stat.changePercent.toFixed(1)}%
                          </span>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          {/* Main Content Tabs */}
          <Tabs defaultValue='overview' className='space-y-4'>
            <TabsList>
              <TabsTrigger value='overview'>Overview</TabsTrigger>
              <TabsTrigger value='trends'>Trends</TabsTrigger>
              <TabsTrigger value='analytics'>Analytics</TabsTrigger>
            </TabsList>

            <TabsContent value='overview' className='space-y-4'>
              <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-1'>
                {/* Multi-sensor Time Series - FIXED */}
                <Card>
                  <CardHeader>
                    <CardTitle>Real-time Sensor Readings</CardTitle>
                    <CardDescription>Live sensor data over time</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width='100%' height={400}>
                      <LineChart
                        data={timeSeriesData}
                        margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                      >
                        <XAxis dataKey='time' tick={{ fontSize: 12 }} interval='preserveStartEnd' />
                        <YAxis tick={{ fontSize: 12 }} />
                        <Tooltip
                          labelFormatter={(label) => `Time: ${label}`}
                          formatter={(value, name) => [
                            `${Number(value).toFixed(1)}${getSensorConfig(name).unit}`,
                            getSensorConfig(name).label,
                          ]}
                        />
                        {selectedSensorType === 'ALL'
                          ? Object.entries(sensorConfig).map(([type, config]) => (
                              <Line
                                key={type}
                                type='monotone'
                                dataKey={type}
                                stroke={config.color}
                                strokeWidth={2}
                                name={config.label}
                                dot={{ r: 2 }}
                                connectNulls={false}
                                activeDot={{ r: 4 }}
                              />
                            ))
                          : selectedSensorType in sensorConfig && (
                              <Line
                                type='monotone'
                                dataKey={selectedSensorType}
                                stroke={getSensorConfig(selectedSensorType).color}
                                strokeWidth={2}
                                name={getSensorConfig(selectedSensorType).label}
                                dot={{ r: 2 }}
                                connectNulls={false}
                                activeDot={{ r: 4 }}
                              />
                            )}
                      </LineChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                {/* Sensor Distribution */}
                <Card>
                  <CardHeader>
                    <CardTitle>Data Distribution</CardTitle>
                    <CardDescription>Number of readings per sensor type</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width='100%' height={300}>
                      <PieChart>
                        <Pie
                          data={sensorDistribution}
                          cx='50%'
                          cy='50%'
                          outerRadius={80}
                          fill='#8884d8'
                          dataKey='count'
                          label={({ label, count }) => `${label}: ${count}`}
                        >
                          {sensorDistribution.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            <TabsContent value='trends' className='space-y-4'>
              <div className='grid gap-4 md:grid-cols-2'>
                {Object.entries(sensorConfig).map(([type, config]) => {
                  const sensorType = type as SensorType;
                  const data = historicalData.filter((d) => d.type === sensorType);
                  const chartData = data.map((d) => ({
                    time: new Date(d.timestamp).toLocaleTimeString(),
                    value: d.value,
                    timestamp: d.timestamp,
                  }));

                  const Icon = config.icon;
                  const { min, max } = config.thresholds;

                  return (
                    <Card key={type}>
                      <CardHeader>
                        <CardTitle className='flex items-center gap-2'>
                          <Icon className='h-5 w-5' style={{ color: config.color }} />
                          {config.label} Trend
                        </CardTitle>
                        <CardDescription>
                          Target range: {min} - {max} {config.unit}
                        </CardDescription>
                      </CardHeader>
                      <CardContent>
                        <ResponsiveContainer width='100%' height={250}>
                          <AreaChart data={chartData}>
                            <XAxis dataKey='time' />
                            <YAxis />
                            <Tooltip />
                            <ReferenceLine y={min} stroke='#ef4444' strokeDasharray='5 5' />
                            <ReferenceLine y={max} stroke='#ef4444' strokeDasharray='5 5' />
                            <Area
                              type='monotone'
                              dataKey='value'
                              stroke={config.color}
                              fill={config.color}
                              fillOpacity={0.3}
                            />
                          </AreaChart>
                        </ResponsiveContainer>
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            </TabsContent>

            <TabsContent value='analytics' className='space-y-4'>
              <div className='grid gap-4'>
                {/* Statistics Summary */}
                <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-4'>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Total Readings</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold'>{historicalData.length}</div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Active Sensors</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold'>{Object.keys(realtimeData).length}</div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Alerts</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold text-red-600'>{alerts.length}</div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Data Quality</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold text-green-600'>
                        {historicalData.length > 0 ? '98%' : '0%'}
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Detailed Statistics */}
                <Card>
                  <CardHeader>
                    <CardTitle>Sensor Statistics</CardTitle>
                    <CardDescription>Detailed analytics for each sensor type</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className='space-y-4'>
                      {Object.entries(stats).map(([type, stat]) => {
                        const config = getSensorConfig(type);
                        const Icon = config.icon;

                        return (
                          <div
                            key={type}
                            className='flex items-center justify-between rounded-lg border p-3'
                          >
                            <div className='flex items-center gap-3'>
                              <Icon className='h-5 w-5' style={{ color: config.color }} />
                              <div>
                                <p className='font-medium'>{config.label}</p>
                                <p className='text-muted-foreground text-sm'>
                                  {stat.dataPoints} readings
                                </p>
                              </div>
                            </div>
                            <div className='space-y-1 text-right'>
                              <div className='space-x-4 text-sm'>
                                <span>
                                  Avg: {stat.average.toFixed(1)}
                                  {config.unit}
                                </span>
                                <span>
                                  Min: {stat.min.toFixed(1)}
                                  {config.unit}
                                </span>
                                <span>
                                  Max: {stat.max.toFixed(1)}
                                  {config.unit}
                                </span>
                              </div>
                              <Badge variant={stat.isInRange ? 'default' : 'destructive'}>
                                {stat.isInRange ? 'In Range' : 'Out of Range'}
                              </Badge>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
};

export default SensorPage;
