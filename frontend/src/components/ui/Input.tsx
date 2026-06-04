import type { InputHTMLAttributes, ReactNode } from 'react';
import { cn } from '../../utils/cn';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  helper?: string;
  icon?: ReactNode;
}

export function Input({ label, error, helper, icon, className, id, required, ...props }: InputProps) {
  const inputId = id ?? props.name ?? label;
  return (
    <label htmlFor={inputId} className="block space-y-1.5">
      <span className="text-sm font-semibold text-slate-700">
        {label} {required && <span className="text-danger-500">*</span>}
      </span>
      <span className="relative block">
        {icon && <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">{icon}</span>}
        <input
          id={inputId}
          className={cn(
            'w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-primary-500 focus:ring-4 focus:ring-primary-100',
            icon && 'pl-10',
            error && 'border-danger-500 focus:border-danger-500 focus:ring-red-100',
            className,
          )}
          required={required}
          {...props}
        />
      </span>
      {helper && !error && <span className="text-xs text-slate-500">{helper}</span>}
      {error && <span className="text-xs font-medium text-danger-500">{error}</span>}
    </label>
  );
}
