import type { ReactNode } from 'react';
import { cn } from '../../utils/cn';

type BadgeTone = 'primary' | 'success' | 'warning' | 'danger' | 'neutral' | 'info';
const tones: Record<BadgeTone, string> = {
  primary: 'bg-primary-50 text-primary-700 ring-primary-100',
  success: 'bg-green-50 text-green-700 ring-green-100',
  warning: 'bg-amber-50 text-amber-700 ring-amber-100',
  danger: 'bg-red-50 text-red-700 ring-red-100',
  neutral: 'bg-slate-100 text-slate-700 ring-slate-200',
  info: 'bg-blue-50 text-blue-700 ring-blue-100',
};

export function Badge({ children, tone = 'neutral', className }: { children: ReactNode; tone?: BadgeTone; className?: string }) {
  return <span className={cn('inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1', tones[tone], className)}>{children}</span>;
}
