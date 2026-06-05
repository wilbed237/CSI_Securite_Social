import { HeartPulse, Sparkles } from 'lucide-react';
import { cn } from '../../utils/cn';

interface CareHealthLogoProps {
  compact?: boolean;
  animated?: boolean;
  className?: string;
}

export function CareHealthLogo({ compact = false, animated = true, className }: CareHealthLogoProps) {
  return (
    <div className={cn('flex items-center gap-3', className)}>
      <div className={cn('care-logo-mark relative grid place-items-center rounded-2xl bg-gradient-to-br from-primary-600 via-accent-500 to-violet-500 text-white shadow-lg shadow-primary-900/20', compact ? 'h-10 w-10' : 'h-14 w-14', animated && 'care-logo-mark-animated')}>
        <HeartPulse className={cn(compact ? 'h-5 w-5' : 'h-7 w-7')} />
        <Sparkles className="absolute -right-1 -top-1 h-4 w-4 rounded-full bg-white/20 p-0.5 text-white" />
      </div>
      {!compact && (
        <div className="leading-tight">
          <p className="text-xl font-black tracking-tight text-current">Care Health</p>
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-current/65">Clinical Suite</p>
        </div>
      )}
    </div>
  );
}
