import type { ReactNode } from 'react';
import { cn } from '../../utils/cn';

type BadgeTone = 'primary' | 'success' | 'warning' | 'danger' | 'neutral' | 'info';
const tones: Record<BadgeTone, string> = {
  primary: 'care-badge-primary',
  success: 'care-badge-success',
  warning: 'care-badge-warning',
  danger: 'care-badge-danger',
  neutral: 'care-badge-neutral',
  info: 'care-badge-info',
};

export function Badge({ children, tone = 'neutral', className }: { children: ReactNode; tone?: BadgeTone; className?: string }) {
  return <span className={cn('inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1', tones[tone], className)}>{children}</span>;
}
