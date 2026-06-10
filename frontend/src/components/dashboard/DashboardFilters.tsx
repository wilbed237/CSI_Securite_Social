import { RotateCcw } from 'lucide-react';
import type { DashboardFilters as Filters } from '../../types/dashboard';
import type { SettingResponse } from '../../types/settings';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Select } from '../ui/Select';

const splitSetting = (settings: SettingResponse[], key: string) => settings.find((item) => item.key === key)?.value.split(',').map((item) => item.trim()).filter(Boolean) ?? [];

export function DashboardFilters({ filters, onChange, onReset, settings }: { filters: Filters; onChange: (filters: Filters) => void; onReset: () => void; settings: SettingResponse[] }) {
  const specialties = splitSetting(settings, 'SPECIALTIES');
  const reimbursementTypes = splitSetting(settings, 'REIMBURSEMENT_TYPES');
  const reimbursementStatuses = splitSetting(settings, 'REIMBURSEMENT_STATUSES');
  return (
    <section aria-label="Filtres du tableau de bord" className="care-card rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-5">
        <Select label="Période" value={filters.period} onChange={(event) => onChange({ ...filters, period: event.target.value as Filters['period'] })} options={[
          { label: "Aujourd'hui", value: 'DAY' }, { label: 'Cette semaine', value: 'WEEK' }, { label: 'Ce mois', value: 'MONTH' },
          { label: 'Trois derniers mois', value: 'QUARTER' }, { label: 'Cette année', value: 'YEAR' }, { label: 'Période personnalisée', value: 'CUSTOM' },
        ]} />
        <Select label="Type de remboursement" value={filters.reimbursementType ?? ''} onChange={(event) => onChange({ ...filters, reimbursementType: event.target.value as Filters['reimbursementType'] || undefined })} options={[{ label: 'Tous', value: '' }, ...reimbursementTypes.map((value) => ({ label: value.replaceAll('_', ' '), value }))]} />
        <Select label="Statut" value={filters.reimbursementStatus ?? ''} onChange={(event) => onChange({ ...filters, reimbursementStatus: event.target.value as Filters['reimbursementStatus'] || undefined })} options={[{ label: 'Tous', value: '' }, ...reimbursementStatuses.map((value) => ({ label: value.replaceAll('_', ' '), value }))]} />
        <Select label="Type de médecin" value={filters.doctorType ?? ''} onChange={(event) => onChange({ ...filters, doctorType: event.target.value as Filters['doctorType'] || undefined, specialty: event.target.value === 'SPECIALIST' ? filters.specialty : undefined })} options={[{ label: 'Tous', value: '' }, { label: 'Généralistes', value: 'GENERALIST' }, { label: 'Spécialistes', value: 'SPECIALIST' }]} />
        <Select label="Spécialité" disabled={filters.doctorType !== 'SPECIALIST'} value={filters.specialty ?? ''} onChange={(event) => onChange({ ...filters, specialty: event.target.value || undefined })} options={[{ label: 'Toutes', value: '' }, ...specialties.map((value) => ({ label: value.replaceAll('_', ' '), value }))]} />
      </div>
      {filters.period === 'CUSTOM' && <div className="mt-3 grid gap-3 sm:grid-cols-2"><Input type="date" label="Date de début" value={filters.startDate ?? ''} onChange={(event) => onChange({ ...filters, startDate: event.target.value })} /><Input type="date" label="Date de fin" value={filters.endDate ?? ''} onChange={(event) => onChange({ ...filters, endDate: event.target.value })} /></div>}
      <div className="mt-4 flex justify-end"><Button type="button" variant="secondary" icon={<RotateCcw className="h-4 w-4" />} onClick={onReset}>Réinitialiser</Button></div>
    </section>
  );
}
