import type { ReactNode } from 'react';

export function PageHeader({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="care-page-title text-2xl font-black tracking-tight text-slate-950 sm:text-3xl">{title}</h1>
        <p className="care-page-description mt-2 max-w-3xl text-sm leading-6 text-slate-600">{description}</p>
      </div>
      {action}
    </div>
  );
}
