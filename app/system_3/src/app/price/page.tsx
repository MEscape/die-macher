'use client';

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '@/store';
import {
  fetchHistoricalPriceData,
  fetchAggregatedPriceData,
  setTimeRange,
  clearError,
} from '@/store/slices/price-slice';

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
  Euro,
  TrendingUp,
  TrendingDown,
  Zap,
  AlertTriangle,
  Clock,
  BarChart3,
  DollarSign,
} from 'lucide-react';

import {
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { PriceData } from '@/types/data';

const PricePage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const {
    historicalData,
    realtimeData,
    error,
    selectedTimeRange,
  }: {
    historicalData: PriceData[];
    aggregatedData: Partial<Record<string, any>>;
    realtimeData: PriceData | null;
    loading: boolean;
    error: string | null;
    selectedTimeRange: string;
  } = useSelector((state: RootState) => state.price);

  useEffect(() => {
    // Fetch initial data
    const now = new Date();
    const start = new Date(now.getTime() - 60 * 60 * 1000).toISOString(); // 1 hour ago
    dispatch(fetchHistoricalPriceData({ start, end: now.toISOString() }));

    // Fetch aggregated data for different metrics
    dispatch(
      fetchAggregatedPriceData({
        field: 'price_per_kwh',
        start: start,
        end: now.toISOString(),
        interval: '15m',
      })
    );
  }, [dispatch]);

  // Debug: Log the first few items to see the data format
  useEffect(() => {
    if (historicalData.length > 0) {
      console.log('Sample price data:', historicalData.slice(0, 3));
      console.log('First startTimestamp:', historicalData[0]?.startTimestamp);
      console.log('First startTimeFormatted:', historicalData[0]?.startTimeFormatted);
    }
  }, [historicalData]);

  const handleTimeRangeChange = (range: string) => {
    dispatch(setTimeRange(range));
    const now = new Date();
    let start: Date;

    switch (range) {
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

    dispatch(fetchHistoricalPriceData({ start: start.toISOString(), end: now.toISOString() }));
  };

  // Analytics functions
  const getPriceStats = () => {
    if (historicalData.length === 0) return null;

    const prices = historicalData.map((d) => d.priceInEurPerKwh);
    const costs = historicalData.map((d) => d.totalCost);

    return {
      avgPrice: prices.reduce((a, b) => a + b, 0) / prices.length,
      minPrice: Math.min(...prices),
      maxPrice: Math.max(...prices),
      totalCost: costs.reduce((a, b) => a + b, 0),
      priceVolatility: Math.sqrt(
        prices.reduce((acc, price, _, arr) => {
          const mean = arr.reduce((a, b) => a + b, 0) / arr.length;
          return acc + Math.pow(price - mean, 2);
        }, 0) / prices.length
      ),
    };
  };

  const getHourlyDistribution = () => {
    const hourlyData = historicalData.reduce(
      (acc, item) => {
        // Use startTimestamp for parsing
        let dateObj;
        try {
          dateObj = new Date(item.startTimestamp);
        } catch (e) {
          console.warn('Failed to parse startTimestamp:', item.startTimestamp);
          return acc;
        }

        if (isNaN(dateObj.getTime())) {
          console.warn('Invalid date:', item.startTimestamp);
          return acc;
        }

        const hour = dateObj.getHours();
        if (!acc[hour]) {
          acc[hour] = { hour, totalCost: 0, avgPrice: 0, count: 0 };
        }
        acc[hour].totalCost += item.totalCost;
        acc[hour].avgPrice += item.priceInEurPerKwh;
        acc[hour].count += 1;
        return acc;
      },
      {} as Record<number, any>
    );

    return Object.values(hourlyData).map((item: any) => ({
      ...item,
      avgPrice: item.count > 0 ? item.avgPrice / item.count : 0,
      hour: `${item.hour.toString().padStart(2, '0')}:00`,
    }));
  };

  const getPriceRangeDistribution = () => {
    const ranges = [
      { label: '< 0.10€', min: 0, max: 0.1, count: 0 },
      { label: '0.10-0.20€', min: 0.1, max: 0.2, count: 0 },
      { label: '0.20-0.30€', min: 0.2, max: 0.3, count: 0 },
      { label: '0.30-0.40€', min: 0.3, max: 0.4, count: 0 },
      { label: '> 0.40€', min: 0.4, max: Infinity, count: 0 },
    ];

    historicalData.forEach((item) => {
      const price = item.priceInEurPerKwh;
      ranges.find((range) => price >= range.min && price < range.max)!.count++;
    });

    return ranges;
  };

  const getRecentPriceChanges = () => {
    return historicalData
      .slice(-10)
      .map((item, index, arr) => {
        const prevPrice = index > 0 ? arr[index - 1].priceInEurPerKwh : item.priceInEurPerKwh;
        const change = item.priceInEurPerKwh - prevPrice;
        const changePercent = prevPrice !== 0 ? (change / prevPrice) * 100 : 0;

        // Use the already formatted time string
        const timeString = item.startTimeFormatted || 'Unknown';

        return {
          ...item,
          priceChange: change,
          changePercent: changePercent,
          time: timeString,
        };
      })
      .reverse();
  };

  const getTrendData = () => {
    return historicalData.map((item, index) => {
      // Use the already formatted time string for display
      const timeString = item.startTimeFormatted || 'Unknown';

      return {
        time: timeString,
        timestamp: item.startTimestamp,
        price: item.priceInEurPerKwh || 0,
        cost: item.totalCost || 0,
        startTime: item.startTimeFormatted || 'N/A',
        endTime: item.endTimeFormatted || 'N/A',
        index: index, // Add index for better sorting/ordering
      };
    });
  };

  const stats = getPriceStats();
  const trendData = getTrendData();
  const hourlyData = getHourlyDistribution();
  const priceRanges = getPriceRangeDistribution();

  const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

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
                <Euro className='text-primary h-8 w-8' />
                <div>
                  <h1 className='text-3xl font-bold'>Price Analytics Dashboard</h1>
                  <p className='text-muted-foreground'>Monitor energy pricing and cost trends</p>
                </div>
              </div>
            </div>

            <div className='flex items-center gap-2'>
              <Select value={selectedTimeRange} onValueChange={handleTimeRangeChange}>
                <SelectTrigger className='w-24'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
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

          {/* Status Cards */}
          <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-2 lg:grid-cols-5'>
            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Current Price</CardTitle>
                <Zap className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>
                  {realtimeData?.priceInEurPerKwh?.toFixed(3) || '0.000'}€/kWh
                </div>
                <p className='text-muted-foreground mt-1 text-xs'>Per kilowatt hour</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Average Price</CardTitle>
                <BarChart3 className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>
                  {stats?.avgPrice?.toFixed(3) || '0.000'}€/kWh
                </div>
                <p className='text-muted-foreground mt-1 text-xs'>Historical average</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Total Cost</CardTitle>
                <DollarSign className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>{stats?.totalCost?.toFixed(2) || '0.00'}€</div>
                <p className='text-muted-foreground mt-1 text-xs'>Period total</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Price Range</CardTitle>
                <TrendingUp className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-lg font-bold'>
                  {stats?.minPrice?.toFixed(3) || '0.000'} -{' '}
                  {stats?.maxPrice?.toFixed(3) || '0.000'}€
                </div>
                <p className='text-muted-foreground mt-1 text-xs'>Min - Max range</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Volatility</CardTitle>
                <TrendingDown className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>
                  {stats?.priceVolatility?.toFixed(4) || '0.0000'}
                </div>
                <p className='text-muted-foreground mt-1 text-xs'>Price volatility</p>
              </CardContent>
            </Card>
          </div>

          {/* Main Content Tabs */}
          <Tabs defaultValue='overview' className='space-y-4'>
            <TabsList>
              <TabsTrigger value='overview'>Overview</TabsTrigger>
              <TabsTrigger value='distribution'>Distribution</TabsTrigger>
            </TabsList>

            <TabsContent value='overview' className='space-y-4'>
              <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-1'>
                {/* Price Trend Chart */}
                <Card className='col-span-2'>
                  <CardHeader>
                    <CardTitle>Price Trend</CardTitle>
                    <CardDescription>Energy price over time</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width='100%' height={400}>
                      <AreaChart data={trendData}>
                        <XAxis
                          dataKey='time'
                          tick={{ fontSize: 12 }}
                          angle={-45}
                          textAnchor='end'
                          height={60}
                        />
                        <YAxis />
                        <Tooltip
                          formatter={(value, name) => [`${Number(value).toFixed(3)}€/kWh`, 'Price']}
                          labelFormatter={(label) => `Time: ${label}`}
                        />
                        <Area
                          type='monotone'
                          dataKey='price'
                          stroke='var(--primary)'
                          fill='var(--primary)'
                          fillOpacity={0.3}
                        />
                      </AreaChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                {/* Hourly Distribution */}
                <Card>
                  <CardHeader>
                    <CardTitle>Hourly Price Distribution</CardTitle>
                    <CardDescription>Average price by hour of day</CardDescription>
                  </CardHeader>
                  <CardContent>
                    {hourlyData.length > 0 ? (
                      <ResponsiveContainer width='100%' height={300}>
                        <BarChart data={hourlyData}>
                          <XAxis dataKey='hour' />
                          <YAxis />
                          <Tooltip
                            formatter={(value) => [`${Number(value).toFixed(3)}€/kWh`, 'Avg Price']}
                          />
                          <Bar dataKey='avgPrice' fill='var(--primary)' radius={[4, 4, 0, 0]} />
                        </BarChart>
                      </ResponsiveContainer>
                    ) : (
                      <div className='text-muted-foreground flex h-[300px] items-center justify-center'>
                        No hourly data available
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>

              {/* Recent Price Changes */}
              <Card>
                <CardHeader>
                  <CardTitle>Recent Price Changes</CardTitle>
                  <CardDescription>Latest price movements</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className='space-y-3'>
                    {getRecentPriceChanges().map((change, index) => (
                      <div
                        key={index}
                        className='flex items-center justify-between rounded-lg border p-3'
                      >
                        <div className='flex items-center gap-3'>
                          <Clock className='text-muted-foreground h-4 w-4' />
                          <div>
                            <p className='font-medium'>{change.time}</p>
                            <p className='text-muted-foreground text-sm'>
                              {change.priceInEurPerKwh.toFixed(3)}€/kWh
                            </p>
                          </div>
                        </div>
                        <div className='flex items-center gap-2'>
                          <Badge
                            variant={
                              change.priceChange > 0.001
                                ? 'destructive'
                                : change.priceChange < -0.001
                                  ? 'default'
                                  : 'secondary'
                            }
                          >
                            {change.priceChange > 0 ? '+' : ''}
                            {change.priceChange.toFixed(4)}€
                          </Badge>
                          {change.changePercent !== 0 && (
                            <span
                              className={`text-sm ${change.changePercent > 0 ? 'text-red-600' : 'text-green-600'}`}
                            >
                              ({change.changePercent > 0 ? '+' : ''}
                              {change.changePercent.toFixed(1)}%)
                            </span>
                          )}
                        </div>
                      </div>
                    ))}
                    {getRecentPriceChanges().length === 0 && (
                      <p className='text-muted-foreground py-4 text-center'>
                        No recent price data available
                      </p>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value='distribution' className='space-y-4'>
              <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-2'>
                {/* Price Range Distribution */}
                <Card>
                  <CardHeader>
                    <CardTitle>Price Range Distribution</CardTitle>
                    <CardDescription>Frequency of different price ranges</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width='100%' height={300}>
                      <PieChart>
                        <Pie
                          data={priceRanges}
                          cx='50%'
                          cy='50%'
                          outerRadius={80}
                          fill='#8884d8'
                          dataKey='count'
                          label={({ label, count }) => `${label}: ${count}`}
                        >
                          {priceRanges.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                {/* Distribution Summary */}
                <Card>
                  <CardHeader>
                    <CardTitle>Distribution Summary</CardTitle>
                    <CardDescription>Price range breakdown</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className='space-y-3'>
                      {priceRanges.map((range, index) => (
                        <div key={index} className='flex items-center justify-between'>
                          <div className='flex items-center gap-2'>
                            <div
                              className='h-4 w-4 rounded-full'
                              style={{ backgroundColor: COLORS[index % COLORS.length] }}
                            />
                            <span className='text-sm font-medium'>{range.label}</span>
                          </div>
                          <div className='flex items-center gap-2'>
                            <span className='text-sm'>{range.count} times</span>
                            <Badge variant='outline'>
                              {historicalData.length > 0
                                ? Math.round((range.count / historicalData.length) * 100)
                                : 0}
                              %
                            </Badge>
                          </div>
                        </div>
                      ))}
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

export default PricePage;
