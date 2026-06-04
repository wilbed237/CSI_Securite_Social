import type { SelectHTMLAttributes } from 'react';
import { cn } from '../../utils/cn';

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string;
  error?: string;
  options: { label: string; value: string }[];
}

export function Select({ label, error, options, className, id, required, ...props }: SelectProps) {
  const selectId = id ?? props.name ?? label;
  return (
    <label htmlFor={selectId} className="block space-y-1.5">
      <span className="text-sm font-semibold text-slate-700">
        {label} {required && <span className="text-danger-500">*</span>}
      </span>
      <select
        id={selectId}
        className={cn(
          'w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition focus:border-primary-500 focus:ring-4 focus:ring-primary-100',
          error && 'border-danger-500 focus:border-danger-500 focus:ring-red-100',
          className,
        )}
        required={required}
        {...props}
      >
        {options.map((option) => (
          <option key={option.value} value={option.value}>{option.label}</option>
        ))}
      </select>
      {error && <span className="text-xs font-medium text-danger-500">{error}</span>}
    </label>
  );
}
