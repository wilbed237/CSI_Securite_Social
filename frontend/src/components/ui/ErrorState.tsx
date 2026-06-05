import { AlertTriangle } from 'lucide-react';
import { Card } from './Card';

export function ErrorState({ title = 'Incident de parcours clinique', message }: { title?: string; message: string }) {
  return (
    <Card className="border-red-100 bg-red-50">
      <div className="flex items-start gap-3 text-red-700">
        <AlertTriangle className="mt-0.5 h-5 w-5 flex-none" />
        <div>
          <h3 className="font-bold">{title}</h3>
          <p className="mt-1 text-sm">{message}</p>
        </div>
      </div>
    </Card>
  );
}
