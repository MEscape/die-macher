'use client';

import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '@/store';
import {
  fetchHistoricalRobotData,
  sendStartProcessingCubes,
  sendCubeManipulation,
  setTimeRange,
  clearError,
} from '@/store/slices/robot-slice';

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
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

import {
  Cpu,
  Activity,
  Box,
  Play,
  RotateCcw,
  AlertTriangle,
  Palette,
  ListTodo,
  BarChart3,
} from 'lucide-react';

import {
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { RobotData } from '@/types/data';

const RobotPage = () => {
  const dispatch = useDispatch<AppDispatch>();
  const {
    historicalData,
    realtimeData,
    loading,
    error,
    selectedTimeRange,
  }: {
    historicalData: RobotData[];
    realtimeData: RobotData | null;
    loading: boolean;
    error: string | null;
    selectedTimeRange: string;
  } = useSelector((state: RootState) => state.robot);

  const [cubeCount, setCubeCount] = useState(5);
  const [selectedColor, setSelectedColor] = useState('RED');
  const [selectedAction, setSelectedAction] = useState('ADD');

  useEffect(() => {
    // Fetch initial data
    const now = new Date();
    const start = new Date(now.getTime() - 60 * 60 * 1000).toISOString(); // 1 hour ago
    dispatch(fetchHistoricalRobotData({ start, end: now.toISOString() }));
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
      default:
        start = new Date(now.getTime() - 60 * 60 * 1000);
    }

    dispatch(fetchHistoricalRobotData({ start: start.toISOString(), end: now.toISOString() }));
  };

  const handleStartProcessing = () => {
    dispatch(sendStartProcessingCubes({ cubeCount }));
  };

  const handleCubeManipulation = () => {
    dispatch(sendCubeManipulation({ color: selectedColor, action: selectedAction }));
  };

  // Analytics functions based on actual data
  const getTaskDistribution = () => {
    const taskCounts = historicalData.reduce(
      (acc, item) => {
        acc[item.robotTask] = (acc[item.robotTask] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );

    return Object.entries(taskCounts).map(([task, count]) => ({
      task,
      count,
    }));
  };

  const getColorDistribution = () => {
    const colorCounts = historicalData.reduce(
      (acc, item) => {
        acc[item.color] = (acc[item.color] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );

    return Object.entries(colorCounts).map(([color, count]) => ({
      color,
      count,
    }));
  };

  const getStatusCounts = () => {
    const statusCounts = historicalData.reduce(
      (acc, item) => {
        acc[item.robotStatus] = (acc[item.robotStatus] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );

    return Object.entries(statusCounts).map(([status, count]) => ({
      status,
      count,
    }));
  };

  const getRecentActivity = () => {
    return historicalData.slice(-5).reverse();
  };

  // Color mappings for charts
  const colorMap: Record<string, string> = {
    RED: '#ef4444',
    BLUE: '#3b82f6',
    GREEN: '#10b981',
    YELLOW: '#f59e0b',
  };

  const distribution = getColorDistribution();

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
                <Cpu className='text-primary h-8 w-8' />
                <div>
                  <h1 className='text-3xl font-bold'>Robot Control Center</h1>
                  <p className='text-muted-foreground'>Monitor and control robot operations</p>
                </div>
              </div>
            </div>

            <div className='flex items-center gap-2'>
              <Select value={selectedTimeRange} onValueChange={handleTimeRangeChange}>
                <SelectTrigger className='w-24'>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='15m'>15m</SelectItem>
                  <SelectItem value='1h'>1h</SelectItem>
                  <SelectItem value='6h'>6h</SelectItem>
                  <SelectItem value='24h'>24h</SelectItem>
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
          <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-2 lg:grid-cols-4'>
            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Current Status</CardTitle>
                <Activity className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>{realtimeData?.robotStatus || 'Unknown'}</div>
                <p className='text-muted-foreground mt-1 text-xs'>Robot operational status</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Current Task</CardTitle>
                <ListTodo className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>{realtimeData?.robotTask || 'None'}</div>
                <p className='text-muted-foreground mt-1 text-xs'>Active robot task</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Current Color</CardTitle>
                <Palette className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='flex items-center gap-2'>
                  <div
                    className='h-6 w-6 rounded-full border-2 border-gray-300'
                    style={{
                      backgroundColor: colorMap[realtimeData?.color || 'gray'] || '#6b7280',
                    }}
                  />
                  <div className='text-2xl font-bold'>{realtimeData?.color || 'None'}</div>
                </div>
                <p className='text-muted-foreground mt-1 text-xs'>Working cube color</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
                <CardTitle className='text-sm font-medium'>Total Records</CardTitle>
                <BarChart3 className='text-muted-foreground h-4 w-4' />
              </CardHeader>
              <CardContent>
                <div className='text-2xl font-bold'>{historicalData.length}</div>
                <p className='text-muted-foreground mt-1 text-xs'>Historical data points</p>
              </CardContent>
            </Card>
          </div>

          {/* Main Content Tabs */}
          <Tabs defaultValue='overview' className='space-y-4'>
            <TabsList>
              <TabsTrigger value='overview'>Overview</TabsTrigger>
              <TabsTrigger value='control'>Control Panel</TabsTrigger>
              <TabsTrigger value='analytics'>Analytics</TabsTrigger>
            </TabsList>

            <TabsContent value='overview' className='space-y-4'>
              <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-2'>
                {/* Status Distribution */}
                <Card className='col-span-1'>
                  <CardHeader>
                    <CardTitle>Status Distribution</CardTitle>
                    <CardDescription>Robot status frequency</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width='100%' height={300}>
                      <BarChart data={getStatusCounts()}>
                        <XAxis dataKey='status' />
                        <YAxis />
                        <Bar dataKey='count' fill='var(--primary)' radius={[4, 4, 0, 0]} />
                      </BarChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>

                {/* Task Distribution */}
                <Card className='col-span-1'>
                  <CardHeader>
                    <CardTitle>Task Distribution</CardTitle>
                    <CardDescription>Frequency of different tasks</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <ResponsiveContainer width='100%' height={300}>
                      <BarChart data={getTaskDistribution()}>
                        <XAxis dataKey='task' />
                        <YAxis />
                        <Bar dataKey='count' fill='var(--primary)' />
                      </BarChart>
                    </ResponsiveContainer>
                  </CardContent>
                </Card>
              </div>

              {/* Recent Activity */}
              <Card>
                <CardHeader>
                  <CardTitle>Recent Activity</CardTitle>
                  <CardDescription>Latest robot operations</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className='space-y-3'>
                    {getRecentActivity().map((activity, index) => (
                      <div
                        key={index}
                        className='flex items-center justify-between rounded-lg border p-3'
                      >
                        <div className='flex items-center gap-3'>
                          <div
                            className='h-4 w-4 rounded-full'
                            style={{ backgroundColor: colorMap[activity.color] || '#6b7280' }}
                          />
                          <div>
                            <p className='font-medium'>{activity.robotTask}</p>
                            <p className='text-muted-foreground text-sm'>
                              {new Date(activity.timestamp).toLocaleString()}
                            </p>
                          </div>
                        </div>
                        <Badge
                          variant={
                            activity.robotStatus === 'active'
                              ? 'default'
                              : activity.robotStatus === 'error'
                                ? 'destructive'
                                : 'secondary'
                          }
                        >
                          {activity.robotStatus}
                        </Badge>
                      </div>
                    ))}
                    {getRecentActivity().length === 0 && (
                      <p className='text-muted-foreground py-4 text-center'>
                        No recent activity data available
                      </p>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value='control' className='space-y-4'>
              <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-2'>
                {/* Cube Processing Control */}
                <Card>
                  <CardHeader>
                    <CardTitle className='flex items-center gap-2'>
                      <Box className='h-5 w-5' />
                      Cube Processing
                    </CardTitle>
                    <CardDescription>Start automated cube processing tasks</CardDescription>
                  </CardHeader>
                  <CardContent className='space-y-4'>
                    <div className='space-y-2'>
                      <Label htmlFor='cubeCount'>Number of Cubes</Label>
                      <Input
                        id='cubeCount'
                        type='number'
                        value={cubeCount}
                        onChange={(e) => setCubeCount(Number(e.target.value))}
                        min={1}
                        max={5}
                      />
                    </div>
                    <Button onClick={handleStartProcessing} disabled={loading} className='w-full'>
                      <Play className='mr-2 h-4 w-4' />
                      Start Processing
                    </Button>
                  </CardContent>
                </Card>

                {/* Manual Cube Control */}
                <Card>
                  <CardHeader>
                    <CardTitle className='flex items-center gap-2'>
                      <RotateCcw className='h-5 w-5' />
                      Manual Cube Control
                    </CardTitle>
                    <CardDescription>Direct cube manipulation commands</CardDescription>
                  </CardHeader>
                  <CardContent className='space-y-4'>
                    <div className='grid grid-cols-2 gap-4'>
                      <div className='space-y-2'>
                        <Label htmlFor='color'>Cube Color</Label>
                        <Select value={selectedColor} onValueChange={setSelectedColor}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value='RED'>Red</SelectItem>
                            <SelectItem value='BLUE'>Blue</SelectItem>
                            <SelectItem value='GREEN'>Green</SelectItem>
                            <SelectItem value='YELLOW'>Yellow</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                      <div className='space-y-2'>
                        <Label htmlFor='action'>Action</Label>
                        <Select value={selectedAction} onValueChange={setSelectedAction}>
                          <SelectTrigger>
                            <SelectValue />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value='ADD'>Add</SelectItem>
                            <SelectItem value='REMOVE'>Remove</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>
                    </div>
                    <Button
                      onClick={handleCubeManipulation}
                      disabled={loading}
                      variant='outline'
                      className='w-full'
                    >
                      Execute Action
                    </Button>
                  </CardContent>
                </Card>
              </div>
            </TabsContent>

            <TabsContent value='analytics' className='space-y-4'>
              <div className='grid gap-4'>
                <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-1'>
                  {/* Color Distribution Pie Chart */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Color Distribution</CardTitle>
                      <CardDescription>Distribution of cube colors processed</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <ResponsiveContainer width='100%' height={300}>
                        <PieChart>
                          <Pie
                            data={distribution}
                            cx='50%'
                            cy='50%'
                            outerRadius={80}
                            fill='#8884d8'
                            dataKey='count'
                            label={({ color, count }) => `${color}: ${count}`}
                          >
                            {distribution.map((entry, index) => (
                              <Cell key={`cell-${index}`} fill={colorMap[entry.color]} />
                            ))}
                          </Pie>
                          <Tooltip />
                        </PieChart>
                      </ResponsiveContainer>
                    </CardContent>
                  </Card>
                </div>

                {/* Summary Statistics */}
                <div className='*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid gap-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs md:grid-cols-4'>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Total Tasks</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold'>{historicalData.length}</div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Unique Tasks</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold'>
                        {new Set(historicalData.map((d) => d.robotTask)).size}
                      </div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Colors Used</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold'>
                        {new Set(historicalData.map((d) => d.color)).size}
                      </div>
                    </CardContent>
                  </Card>
                  <Card>
                    <CardHeader>
                      <CardTitle className='text-sm'>Error Rate</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className='text-2xl font-bold'>
                        {historicalData.length > 0
                          ? Math.round(
                              (historicalData.filter((d) => d.robotStatus === 'error').length /
                                historicalData.length) *
                                100
                            )
                          : 0}
                        %
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
};

export default RobotPage;
