import type { TextareaHTMLAttributes } from 'react';
import { cn } from '../../utils/cn';

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
}

export function Textarea({ label, error, className, id, required, ...props }: TextareaProps) {
  const textareaId = id ?? props.name ?? label;
  return (
    <label htmlFor={textareaId} className="block space-y-1.5">
      <span className="text-sm font-semibold text-slate-700">
        {label} {required && <span className="text-danger-500">*</span>}
      </span>
      <textarea
        id={textareaId}
        className={cn(
          'min-h-28 w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-primary-500 focus:ring-4 focus:ring-primary-100',
          error && 'border-danger-500 focus:border-danger-500 focus:ring-red-100',
          className,
        )}
        required={required}
        {...props}
      />
      {error && <span className="text-xs font-medium text-danger-500">{error}</span>}
    </label>
  );
}
