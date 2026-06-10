import type { KeyboardEvent, ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';

interface Props {
  title: string;
  value: string;
  description: string;
  icon: ReactNode;
  to: string;
  ariaLabel?: string;
}

export function ClickableStatCard({ title, value, description, icon, to, ariaLabel }: Props) {
  const navigate = useNavigate();
  const activate = () => navigate(to);
  const onKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      activate();
    }
  };

  return (
    <div role="link" tabIndex={0} aria-label={ariaLabel ?? title} onClick={activate} onKeyDown={onKeyDown}
      className="care-card care-stat-card cursor-pointer rounded-lg border border-slate-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-primary-300 hover:shadow-md focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 active:translate-y-0">
      <div className="flex items-start justify-between gap-4">
        <div><p className="text-sm font-medium text-slate-500">{title}</p><p className="mt-2 text-3xl font-black text-slate-950">{value}</p></div>
        <div className="care-stat-icon rounded-lg bg-secondary-100 p-3 text-primary-700">{icon}</div>
      </div>
      <p className="mt-4 text-sm text-slate-500">{description}</p>
    </div>
  );
}
