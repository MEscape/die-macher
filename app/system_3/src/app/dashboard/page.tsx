'use client';

import React from 'react';

import { AppSidebar } from '@/components/app-sidebar';
import { SiteHeader } from '@/components/site-header';
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar';

import { BarChart3 } from 'lucide-react';

// Import your existing components
import { MqttStatus } from '@/components/mqtt-status';
import { SectionCards } from '@/components/section-cards';
import { ChartAreaInteractive } from '@/components/chart-area-interactive';
import { DataTable } from '@/components/data-table';

export default function Page() {
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
                <BarChart3 className='text-primary h-8 w-8' />
                <div>
                  <h1 className='text-3xl font-bold'>Dashboard Overview</h1>
                  <p className='text-muted-foreground'>Monitor system metrics and performance</p>
                </div>
              </div>
            </div>
          </div>

          {/* MQTT Status */}
          <div className='px-0'>
            <MqttStatus variant='detailed' />
          </div>

          {/* Main Content */}
          <div className='flex flex-1 flex-col'>
            <div className='@container/main flex flex-1 flex-col gap-2'>
              <div className='flex flex-col gap-4 md:gap-6'>
                <SectionCards />
                <div className='px-0'>
                  <ChartAreaInteractive />
                </div>
                <DataTable />
              </div>
            </div>
          </div>
        </div>
      </SidebarInset>
    </SidebarProvider>
  );
}
