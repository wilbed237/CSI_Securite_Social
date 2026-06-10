import type { ReactNode } from 'react';
import { FileSearch } from 'lucide-react';
import { Card } from './Card';
import { useTranslation } from '../../i18n';

export function EmptyState({ title = 'Aucune donnée clinique', description = 'Aucun élément ne correspond aux critères actuels.', action }: { title?: string; description?: string; action?: ReactNode }) {
  const { t } = useTranslation();
  const visibleTitle = title === 'Aucune donnée clinique' ? t('states.emptyTitle', title) : title;
  const visibleDescription = description === 'Aucun élément ne correspond aux critères actuels.' ? t('states.emptyDescription', description) : description;
  return (
    <Card className="text-center">
      <div className="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-primary-50 text-primary-700">
        <FileSearch className="h-6 w-6" />
      </div>
      <h3 className="mt-4 text-base font-black text-slate-950">{visibleTitle}</h3>
      <p className="mx-auto mt-2 max-w-md text-sm text-slate-500">{visibleDescription}</p>
      {action && <div className="mt-5">{action}</div>}
    </Card>
  );
}
