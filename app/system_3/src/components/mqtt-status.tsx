'use client';

import React from 'react';
import { cn } from '@/lib/utils';
import { Badge } from '@/components/ui/badge';
import { Wifi, WifiOff, Activity } from 'lucide-react';
import { useMqttContext } from '@/providers/mqtt-provider';

interface MqttStatusProps {
  className?: string;
  variant?: 'default' | 'compact' | 'detailed';
}

export function MqttStatus({ className, variant = 'default' }: MqttStatusProps) {
  const { isConnected } = useMqttContext();

  if (variant === 'compact') {
    return (
      <div className={cn('flex items-center gap-2', className)}>
        <div className='relative h-2 w-2'>
          <div
            className={cn(
              'h-full w-full rounded-full transition-all duration-300',
              isConnected
                ? 'animate-pulse bg-[var(--primary)] shadow-[var(--primary)/50] shadow-md'
                : 'bg-muted-foreground/50'
            )}
          />
          {isConnected && (
            <div className='absolute inset-0 h-full w-full animate-ping rounded-full bg-[var(--primary)] opacity-75' />
          )}
        </div>
        <span className='text-xs font-medium text-[var(--primary)]'>MQTT</span>
      </div>
    );
  }

  if (variant === 'detailed') {
    return (
      <div
        className={cn(
          'flex items-center gap-3 rounded-lg border p-3 transition-all duration-300',
          'border-[var(--primary)] bg-[var(--primary)/10] dark:bg-[var(--primary)/10]',
          className
        )}
      >
        <div className='relative'>
          {isConnected ? (
            <Wifi className='h-5 w-5 animate-pulse text-[var(--primary)]' />
          ) : (
            <WifiOff className='text-muted-foreground h-5 w-5' />
          )}
          {isConnected && (
            <div className='absolute -top-1 -right-1'>
              <Activity className='h-3 w-3 animate-ping text-[var(--primary)]' />
            </div>
          )}
        </div>
        <div className='flex flex-col'>
          <span className='text-sm font-medium text-[var(--primary)]'>MQTT Broker</span>
          <span className='text-xs text-[var(--primary)]'>
            {isConnected ? 'Connected & Live' : 'Disconnected'}
          </span>
        </div>
        <Badge
          variant='outline'
          className='ml-auto border-[var(--primary)] text-xs text-[var(--primary)]'
        >
          {isConnected ? 'LIVE' : 'OFFLINE'}
        </Badge>
      </div>
    );
  }

  // Default variant
  return (
    <Badge
      variant='outline'
      className={cn(
        'flex items-center gap-2 px-3 py-1.5 transition-all duration-300',
        'bg-[var(--primary)] text-white shadow-[var(--primary)/25] shadow-lg hover:bg-[var(--primary)]',
        className
      )}
    >
      <div className='relative flex items-center'>
        <div className='relative h-3.5 w-3.5'>
          <div
            className={cn(
              'h-full w-full rounded-full',
              isConnected ? 'animate-pulse bg-white/90' : 'bg-white/50'
            )}
          />
          {isConnected && (
            <div className='absolute inset-0 h-full w-full animate-ping rounded-full border border-white/30' />
          )}
        </div>
      </div>
      <span className='text-xs font-medium'>MQTT {isConnected ? 'LIVE' : 'OFFLINE'}</span>
    </Badge>
  );
}
