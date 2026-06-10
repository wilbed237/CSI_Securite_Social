import { AlertTriangle } from 'lucide-react';
import { Card } from './Card';
import { useTranslation } from '../../i18n';

export function ErrorState({ title = 'Incident de parcours clinique', message }: { title?: string; message: string }) {
  const { t } = useTranslation();
  return (
    <Card className="care-error-state">
      <div className="flex items-start gap-3">
        <AlertTriangle className="mt-0.5 h-5 w-5 flex-none" />
        <div>
          <h3 className="font-bold">{title === 'Incident de parcours clinique' ? t('states.errorTitle', title) : title}</h3>
          <p className="mt-1 text-sm">{message}</p>
        </div>
      </div>
    </Card>
  );
}
