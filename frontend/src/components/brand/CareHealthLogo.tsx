import { cn } from '../../utils/cn';

interface CareHealthLogoProps {
  compact?: boolean;
  animated?: boolean;
  className?: string;
}

function CareHealthMark({ size = 48, className }: { size?: number; className?: string }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 48 48"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={className}
      aria-hidden="true"
      role="img"
    >
      {/* Bouclier arrondi */}
      <path
        d="M24 4L8 10V24C8 33.6 15.2 42.4 24 45C32.8 42.4 40 33.6 40 24V10L24 4Z"
        fill="url(#shield-gradient)"
      />
      {/* Croix médicale */}
      <rect x="20" y="14" width="8" height="20" rx="2" fill="white" opacity="0.95" />
      <rect x="14" y="20" width="20" height="8" rx="2" fill="white" opacity="0.95" />
      {/* Ligne ECG stylisée */}
      <path
        d="M10 28 L15 28 L17 23 L20 33 L23 26 L26 30 L28 28 L38 28"
        stroke="white"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
        opacity="0.7"
        fill="none"
      />
      <defs>
        <linearGradient id="shield-gradient" x1="8" y1="4" x2="40" y2="45" gradientUnits="userSpaceOnUse">
          <stop offset="0%" stopColor="#2563EB" />
          <stop offset="55%" stopColor="#1D4ED8" />
          <stop offset="100%" stopColor="#7C3AED" />
        </linearGradient>
      </defs>
    </svg>
  );
}

export function CareHealthLogo({ compact = false, animated = true, className }: CareHealthLogoProps) {
  return (
    <div className={cn('flex items-center gap-3', className)}>
      <div
        className={cn(
          'relative shrink-0',
          animated && 'transition-transform duration-300 hover:scale-105',
        )}
      >
        <CareHealthMark size={compact ? 38 : 48} />
      </div>
      {!compact && (
        <div className="leading-tight select-none">
          <p className="text-xl font-black tracking-tight text-current">Care Health</p>
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-current/60">Clinical Suite</p>
        </div>
      )}
    </div>
  );
}

export { CareHealthMark };
