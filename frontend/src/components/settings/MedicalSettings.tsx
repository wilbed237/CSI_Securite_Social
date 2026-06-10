import type { SettingResponse } from '../../types/settings';
import { Badge } from '../ui/Badge';

export function MedicalSettings({ settings }: { settings: SettingResponse[] }) {
  if (settings.length === 0) return <p className="text-sm text-slate-500">Aucun référentiel médical configuré.</p>;
  return (
    <div className="grid gap-3">
      {settings.map((setting) => (
        <div key={setting.id} className="rounded-xl border border-slate-200 p-4">
          <div className="flex items-center justify-between gap-3">
            <p className="font-bold text-slate-950">{setting.key}</p>
            <Badge tone={setting.active ? 'success' : 'warning'}>{setting.active ? 'Actif' : 'Inactif'}</Badge>
          </div>
          <p className="mt-2 text-sm text-slate-600">{setting.value}</p>
          {setting.description && <p className="mt-1 text-xs text-slate-500">{setting.description}</p>}
        </div>
      ))}
    </div>
  );
}
