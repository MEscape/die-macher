import type { Metadata } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import './globals.css';
import { ThemeProvider } from '@/providers/theme-provider';
import { ActiveThemeProvider } from '@/components/active-theme';
import { cookies } from 'next/headers';
import { cn } from '@/lib/utils';
import ReduxProvider from '@/providers/provider';
import { MqttProvider } from '@/providers/mqtt-provider';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  title: 'Die Macher – Smart Factory Control',
  description:
    'Visualize, monitor, and control robotic systems in real time. Built for precision automation with modular architecture and insightful dashboards.',
};

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const cookieStore = await cookies();
  const activeThemeValue = cookieStore.get('active_theme')?.value;
  const isScaled = activeThemeValue?.endsWith('-scaled');

  return (
    <html lang='en' suppressHydrationWarning>
      <body
        className={cn(
          `bg-background overscroll-none ${geistSans.variable} ${geistMono.variable} antialiased`,
          activeThemeValue ? `theme-${activeThemeValue}` : '',
          isScaled ? 'theme-scaled' : ''
        )}
      >
        <ReduxProvider>
          <MqttProvider>
            <ThemeProvider
              attribute='class'
              defaultTheme='system'
              enableSystem
              disableTransitionOnChange
              enableColorScheme
            >
              <ActiveThemeProvider initialTheme={activeThemeValue}>{children}</ActiveThemeProvider>
            </ThemeProvider>
          </MqttProvider>
        </ReduxProvider>
      </body>
    </html>
  );
}
