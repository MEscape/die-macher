'use client';

import * as React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import {
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
  CheckCircle2,
  Loader2,
  AlertCircle,
  Circle,
  Columns,
} from 'lucide-react';
import {
  ColumnDef,
  ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  getFacetedRowModel,
  getFacetedUniqueValues,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  SortingState,
  useReactTable,
  VisibilityState,
} from '@tanstack/react-table';
import { z } from 'zod';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { fetchHistoricalRobotData } from '@/store/slices/robot-slice';
import { selectHistoricalRobotData } from '@/store/selector/robot';
import { RootState } from '@/store';

export const robotSchema = z.object({
  id: z.number(),
  robotTask: z.string(),
  robotStatus: z.string(),
  color: z.string().optional(),
  timestamp: z.string(),
  metadata: z.object({}).optional(),
});

const getStatusIcon = (status: string) => {
  switch (status) {
    case 'OK':
      return <CheckCircle2 className='h-4 w-4 text-slate-600' />;
    case 'OFFLINE':
      return <AlertCircle className='h-4 w-4 text-slate-600' />;
    case 'BUSY':
      return <Loader2 className='h-4 w-4 animate-spin text-slate-600' />;
    default:
      return <Circle className='h-4 w-4 text-slate-400' />;
  }
};

const getTaskBadgeVariant = (task: string) => {
  switch (task) {
    case 'PICK':
      return 'secondary';
    case 'ERROR':
      return 'secondary';
    case 'IDLE':
      return 'secondary';
    default:
      return 'secondary';
  }
};

const getColorIndicator = (color?: string) => {
  if (!color) {
    return <span className='text-muted-foreground text-sm'>None</span>;
  }

  const colorMap: Record<string, string> = {
    RED: 'bg-red-500',
    YELLOW: 'bg-yellow-500',
    GREEN: 'bg-green-500',
    BLUE: 'bg-blue-500',
    PURPLE: 'bg-purple-500',
    ORANGE: 'bg-orange-500',
  };

  return (
    <div className='flex items-center gap-2'>
      <div className={`h-3 w-3 rounded-full ${colorMap[color] || 'bg-gray-500'}`} />
      <span className='text-sm'>{color}</span>
    </div>
  );
};

const formatTimestamp = (timestamp: string) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

  if (diffInMinutes < 1) return 'Just now';
  if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
  if (diffInMinutes < 1440) return `${Math.floor(diffInMinutes / 60)}h ago`;

  return (
    date.toLocaleDateString() +
    ' ' +
    date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  );
};

const columns: ColumnDef<z.infer<typeof robotSchema>>[] = [
  {
    accessorKey: 'robotTask',
    header: 'Task',
    cell: ({ row }) => (
      <Badge variant={getTaskBadgeVariant(row.original.robotTask)} className='font-medium'>
        {row.original.robotTask}
      </Badge>
    ),
    enableHiding: false,
  },
  {
    accessorKey: 'robotStatus',
    header: 'Status',
    cell: ({ row }) => (
      <div className='flex items-center gap-2'>
        {getStatusIcon(row.original.robotStatus)}
        <span className='font-medium'>{row.original.robotStatus}</span>
      </div>
    ),
  },
  {
    accessorKey: 'color',
    header: 'Color',
    cell: ({ row }) => getColorIndicator(row.original.color),
  },
  {
    accessorKey: 'timestamp',
    header: 'Last Update',
    cell: ({ row }) => (
      <div className='text-sm'>
        <div className='font-medium'>{formatTimestamp(row.original.timestamp)}</div>
        <div className='text-muted-foreground text-xs'>
          {new Date(row.original.timestamp).toLocaleTimeString()}
        </div>
      </div>
    ),
  },
];

export function DataTable() {
  const dispatch = useDispatch();
  const initialData: z.infer<typeof robotSchema>[] = useSelector((state: RootState) =>
    selectHistoricalRobotData(state)
  );

  const [data, setData] = React.useState(() =>
    initialData.map((item, index) => ({ ...item, id: index + 1 }))
  );

  React.useEffect(() => {
    const now = new Date();
    const start = new Date(now.getTime() - 24 * 60 * 60 * 1000); // 24 hours ago

    dispatch(
      fetchHistoricalRobotData({
        start: start.toISOString(),
        end: now.toISOString(),
      })
    );
  }, [dispatch]);

  React.useEffect(() => {
    setData(initialData.map((item, index) => ({ ...item, id: index + 1 })));
  }, [initialData]);

  const [columnVisibility, setColumnVisibility] = React.useState<VisibilityState>({});
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const [sorting, setSorting] = React.useState<SortingState>([
    {
      id: 'timestamp',
      desc: true, // Show latest first
    },
  ]);
  const [pagination, setPagination] = React.useState({
    pageIndex: 0,
    pageSize: 10,
  });

  const table = useReactTable({
    data,
    columns,
    state: {
      sorting,
      columnVisibility,
      columnFilters,
      pagination,
    },
    onSortingChange: setSorting,
    onColumnFiltersChange: setColumnFilters,
    onColumnVisibilityChange: setColumnVisibility,
    onPaginationChange: setPagination,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFacetedRowModel: getFacetedRowModel(),
    getFacetedUniqueValues: getFacetedUniqueValues(),
  });

  // Filter by status for quick access
  const statusCounts = React.useMemo(() => {
    return data.reduce(
      (acc, item) => {
        acc[item.robotStatus] = (acc[item.robotStatus] || 0) + 1;
        return acc;
      },
      {} as Record<string, number>
    );
  }, [data]);

  return (
    <div className='w-full flex-col justify-start gap-6'>
      <div className='flex items-center justify-between p-4'>
        <div className='flex items-center gap-4'>
          <h2 className='text-2xl font-bold'>Robot Status Dashboard</h2>
          <div className='flex gap-2'>
            <Badge variant='outline' className='gap-1'>
              <CheckCircle2 className='h-3 w-3 text-slate-600' />
              OK: {statusCounts.OK || 0}
            </Badge>
            <Badge variant='outline' className='gap-1'>
              <Loader2 className='h-3 w-3 text-slate-600' />
              BUSY: {statusCounts.BUSY || 0}
            </Badge>
            <Badge variant='outline' className='gap-1'>
              <AlertCircle className='h-3 w-3 text-slate-600' />
              OFFLINE: {statusCounts.OFFLINE || 0}
            </Badge>
          </div>
        </div>
        <div className='flex items-center gap-2'>
          <Input
            placeholder='Filter robot task...'
            value={(table.getColumn('robotTask')?.getFilterValue() as string) ?? ''}
            onChange={(event) => table.getColumn('robotTask')?.setFilterValue(event.target.value)}
            className='max-w-sm'
          />
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='sm'>
                <Columns className='h-4 w-4' />
                Columns
                <ChevronDown className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end' className='w-56'>
              {table
                .getAllColumns()
                .filter((column) => typeof column.accessorFn !== 'undefined' && column.getCanHide())
                .map((column) => {
                  return (
                    <DropdownMenuCheckboxItem
                      key={column.id}
                      className='capitalize'
                      checked={column.getIsVisible()}
                      onCheckedChange={(value) => column.toggleVisibility(value)}
                    >
                      {column.id}
                    </DropdownMenuCheckboxItem>
                  );
                })}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      <div className='relative flex flex-col gap-4 overflow-auto px-4'>
        <div className='overflow-hidden rounded-lg border'>
          <Table>
            <TableHeader className='bg-muted sticky top-0 z-10'>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => {
                    return (
                      <TableHead key={header.id} colSpan={header.colSpan}>
                        {header.isPlaceholder
                          ? null
                          : flexRender(header.column.columnDef.header, header.getContext())}
                      </TableHead>
                    );
                  })}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody>
              {table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row) => (
                  <TableRow key={row.id} data-state={row.getIsSelected() && 'selected'}>
                    {row.getVisibleCells().map((cell) => (
                      <TableCell key={cell.id}>
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={columns.length} className='h-24 text-center'>
                    No robots found.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
        <div className='flex items-center justify-between px-4'>
          <div className='text-muted-foreground hidden flex-1 text-sm lg:flex'>
            {table.getFilteredRowModel().rows.length} robot(s) total.
          </div>
          <div className='flex w-full items-center gap-8 lg:w-fit'>
            <div className='hidden items-center gap-2 lg:flex'>
              <Label htmlFor='rows-per-page' className='text-sm font-medium'>
                Rows per page
              </Label>
              <Select
                value={`${table.getState().pagination.pageSize}`}
                onValueChange={(value) => {
                  table.setPageSize(Number(value));
                }}
              >
                <SelectTrigger className='h-8 w-[70px]' id='rows-per-page'>
                  <SelectValue placeholder={table.getState().pagination.pageSize} />
                </SelectTrigger>
                <SelectContent side='top'>
                  {[10, 20, 30, 40, 50].map((pageSize) => (
                    <SelectItem key={pageSize} value={`${pageSize}`}>
                      {pageSize}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className='flex w-fit items-center justify-center text-sm font-medium'>
              Page {table.getState().pagination.pageIndex + 1} of {table.getPageCount()}
            </div>
            <div className='ml-auto flex items-center gap-2 lg:ml-0'>
              <Button
                variant='outline'
                className='hidden h-8 w-8 p-0 lg:flex'
                onClick={() => table.setPageIndex(0)}
                disabled={!table.getCanPreviousPage()}
              >
                <span className='sr-only'>Go to first page</span>
                <ChevronsLeft className='h-4 w-4' />
              </Button>
              <Button
                variant='outline'
                className='h-8 w-8 p-0'
                onClick={() => table.previousPage()}
                disabled={!table.getCanPreviousPage()}
              >
                <span className='sr-only'>Go to previous page</span>
                <ChevronLeft className='h-4 w-4' />
              </Button>
              <Button
                variant='outline'
                className='h-8 w-8 p-0'
                onClick={() => table.nextPage()}
                disabled={!table.getCanNextPage()}
              >
                <span className='sr-only'>Go to next page</span>
                <ChevronRight className='h-4 w-4' />
              </Button>
              <Button
                variant='outline'
                className='hidden h-8 w-8 p-0 lg:flex'
                onClick={() => table.setPageIndex(table.getPageCount() - 1)}
                disabled={!table.getCanNextPage()}
              >
                <span className='sr-only'>Go to last page</span>
                <ChevronsRight className='h-4 w-4' />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
