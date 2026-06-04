import { useQuery } from '@tanstack/react-query';
import { Plus, Search } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { RoleGate } from '../../components/RoleGate';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { DataTable } from '../../components/ui/Table';
import { ErrorMessage } from '../../components/ui/ErrorMessage';
import { Loader } from '../../components/ui/Loader';
import { Select } from '../../components/ui/Select';
import { usePageTitle } from '../../hooks/usePageTitle';
import type { DoctorType } from '../../types/api';

export function DoctorListPage() {
  usePageTitle('Médecins');
  const [type, setType] = useState<DoctorType | ''>('');
  const query = useQuery({ queryKey: ['doctors', type], queryFn: () => profileApi.listDoctors({ type: type || undefined, page: 0, size: 20 }) });

  return (
    <div>
      <PageHeader title="Médecins" description="Annuaire des médecins généralistes et spécialistes enregistrés." action={<RoleGate roles={['AGENT']}><Link to="/app/doctors/new"><Button icon={<Plus className="h-4 w-4" />}>Nouveau médecin</Button></Link></RoleGate>} />
      <div className="mb-4 max-w-xs"><Select label="Filtrer par type" value={type} onChange={(e) => setType(e.target.value as DoctorType | '')} options={[{ label: 'Tous', value: '' }, { label: 'Généralistes', value: 'GENERALIST' }, { label: 'Spécialistes', value: 'SPECIALIST' }]} /></div>
      {query.isLoading && <Loader />}
      {query.isError && <ErrorMessage message={extractApiError(query.error)} />}
      {query.data && (
        <DataTable headers={['Matricule', 'Nom', 'Type', 'Spécialité', 'Contact']} empty="Aucun médecin trouvé.">
          {query.data.content.map((doctor) => (
            <tr key={doctor.id}>
              <td className="px-4 py-3 font-semibold text-slate-900">{doctor.matricule}</td>
              <td className="px-4 py-3">Dr {doctor.firstName} {doctor.lastName}</td>
              <td className="px-4 py-3"><Badge tone={doctor.type === 'GENERALIST' ? 'success' : 'info'}>{doctor.type}</Badge></td>
              <td className="px-4 py-3">{doctor.specialty ?? '-'}</td>
              <td className="px-4 py-3 text-slate-500">{doctor.email ?? doctor.phoneNumber ?? '-'}</td>
            </tr>
          ))}
        </DataTable>
      )}
      <p className="mt-3 flex items-center gap-2 text-xs text-slate-500"><Search className="h-4 w-4" /> Pagination backend disponible via page/size.</p>
    </div>
  );
}
