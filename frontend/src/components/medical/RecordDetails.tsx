import type { ReactNode } from 'react';

export function RecordDetails({ children }: { children: ReactNode }) {
  return <dl className="grid gap-4 md:grid-cols-2">{children}</dl>;
}

export function DetailRow({ label, children }: { label: string; children: ReactNode }) {
  return <div className="min-w-0 rounded-xl bg-slate-50 p-4 dark:bg-slate-900/40"><dt className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</dt><dd className="mt-1 break-words text-sm text-slate-900 dark:text-slate-100">{children ?? '—'}</dd></div>;
}
