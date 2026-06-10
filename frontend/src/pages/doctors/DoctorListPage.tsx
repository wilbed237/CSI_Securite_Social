import { useQuery } from '@tanstack/react-query';
import { Plus, Search } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { RoleGate } from '../../components/RoleGate';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { DataTable } from '../../components/ui/Table';
import { ErrorState } from '../../components/ui/ErrorState';
import { Loader } from '../../components/ui/Loader';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { usePageTitle } from '../../hooks/usePageTitle';
import type { DoctorType } from '../../types/api';

export function DoctorListPage() {
  usePageTitle('Médecins');
  const [params, setParams] = useSearchParams();
  const type = (params.get('type') as DoctorType) || '';
  const active = params.get('active') ?? '';
  const specialty = params.get('specialty') ?? '';
  const query = useQuery({ queryKey: ['doctors', type, active, specialty], queryFn: () => profileApi.listDoctors({ type: type || undefined, active: active === '' ? undefined : active === 'true', specialty: specialty || undefined, page: 0, size: 50 }) });
  const setFilter = (key: string, value: string) => { const next = new URLSearchParams(params); if (value) next.set(key, value); else next.delete(key); setParams(next, { replace: true }); };

  return (
    <div>
      <PageHeader title="Médecins" description="Annuaire des médecins généralistes et spécialistes enregistrés." action={<RoleGate roles={['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'ADMIN']}><Link to="/app/doctors/new"><Button icon={<Plus className="h-4 w-4" />}>Nouveau médecin</Button></Link></RoleGate>} />
      <div className="mb-4 grid gap-3 md:grid-cols-3"><Select label="Type" value={type} onChange={(e) => setFilter('type', e.target.value)} options={[{ label: 'Tous', value: '' }, { label: 'Généralistes', value: 'GENERALIST' }, { label: 'Spécialistes', value: 'SPECIALIST' }]} /><Select label="Service" value={active} onChange={(e) => setFilter('active', e.target.value)} options={[{ label: 'Tous', value: '' }, { label: 'En service', value: 'true' }, { label: 'Hors service', value: 'false' }]} /><Input label="Spécialité" value={specialty} onChange={(e) => setFilter('specialty', e.target.value)} /></div>
      {query.isLoading && <Loader />}
      {query.isError && <ErrorState message={extractApiError(query.error)} />}
      {query.data && (
        <DataTable headers={['Matricule', 'Nom', 'Type', 'Spécialité', 'Contact']} empty="Aucun médecin trouvé.">
          {query.data.content.map((doctor) => (
            <tr key={doctor.id} className="care-table-row">
              <td className="care-table-cell-strong px-4 py-3 font-semibold">{doctor.matricule}</td>
              <td className="care-table-cell px-4 py-3">Dr {doctor.firstName} {doctor.lastName}</td>
              <td className="px-4 py-3"><Badge tone={doctor.type === 'GENERALIST' ? 'success' : 'info'}>{doctor.type}</Badge></td>
              <td className="care-table-cell px-4 py-3">{doctor.specialty ?? '-'}</td>
              <td className="care-table-cell-muted px-4 py-3">{doctor.email ?? doctor.phoneNumber ?? '-'}</td>
            </tr>
          ))}
        </DataTable>
      )}
      <p className="mt-3 flex items-center gap-2 text-xs text-slate-500"><Search className="h-4 w-4" /> Pagination backend disponible via page/size.</p>
    </div>
  );
}
