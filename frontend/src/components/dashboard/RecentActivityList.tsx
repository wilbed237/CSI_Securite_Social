import { Card, CardHeader } from '../ui/Card';
import { EmptyState } from '../ui/EmptyState';

export interface RecentActivityItem {
  id: string;
  title: string;
  description?: string;
  meta?: string;
}

export function RecentActivityList({ title, items }: { title: string; items: RecentActivityItem[] }) {
  if (items.length === 0) {
    return <EmptyState title={title} description="Aucune activité récente disponible." />;
  }
  return (
    <Card>
      <CardHeader title={title} />
      <div className="divide-y divide-slate-100">
        {items.map((item) => (
          <div key={item.id} className="py-3">
            <p className="font-semibold text-slate-950">{item.title}</p>
            {item.description && <p className="mt-1 text-sm text-slate-500">{item.description}</p>}
            {item.meta && <p className="mt-1 text-xs font-semibold uppercase text-primary-700">{item.meta}</p>}
          </div>
        ))}
      </div>
    </Card>
  );
}
