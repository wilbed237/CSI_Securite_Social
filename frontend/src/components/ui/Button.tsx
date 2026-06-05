import type { ButtonHTMLAttributes, ReactNode } from 'react';
import { cn } from '../../utils/cn';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger' | 'success';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  isLoading?: boolean;
  icon?: ReactNode;
}

const variants: Record<ButtonVariant, string> = {
  primary: 'bg-gradient-to-r from-primary-700 via-primary-600 to-accent-500 text-white hover:brightness-110 focus-visible:outline-primary-600',
  secondary: 'bg-secondary-100 text-primary-900 hover:bg-primary-100 focus-visible:outline-primary-500 dark:bg-slate-800 dark:text-slate-100 dark:hover:bg-slate-700',
  ghost: 'bg-transparent text-slate-700 hover:bg-slate-100 focus-visible:outline-slate-400',
  danger: 'bg-gradient-to-r from-danger-500 to-accent-600 text-white hover:brightness-110 focus-visible:outline-danger-500',
  success: 'bg-gradient-to-r from-success-500 to-secondary-100 text-slate-950 hover:brightness-105 focus-visible:outline-success-500',
};

export function Button({ className, variant = 'primary', isLoading, icon, children, disabled, ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        'button-press inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold shadow-md shadow-primary-900/10 transition duration-200 hover:-translate-y-0.5 active:translate-y-0 active:scale-95 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 disabled:cursor-not-allowed disabled:opacity-60',
        variants[variant],
        className,
      )}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" /> : icon}
      {children}
    </button>
  );
}
