import { useQuery } from '@tanstack/react-query';
import { Search, UserPlus } from 'lucide-react';
import { Link, useSearchParams } from 'react-router-dom';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { RoleGate } from '../../components/RoleGate';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { ErrorState } from '../../components/ui/ErrorState';
import { Input } from '../../components/ui/Input';
import { Loader } from '../../components/ui/Loader';
import { Select } from '../../components/ui/Select';
import { DataTable } from '../../components/ui/Table';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useAuthStore } from '../../store/authStore';
import type { InsuredStatus } from '../../types/api';

export function InsuredListPage() {
  usePageTitle('Patients couverts');
  const isDoctor = useAuthStore((s) => s.hasAnyRole(['DOCTOR', 'GENERALIST', 'SPECIALIST']));
  const [params, setParams] = useSearchParams();
  const status = (params.get('status') as InsuredStatus) || '';
  const search = params.get('search') ?? '';
  const agentQuery = useQuery({ queryKey: ['insured', status, search], queryFn: () => profileApi.listInsured({ status: status || undefined, search: search || undefined, page: 0, size: 50 }), enabled: !isDoctor });
  const doctorQuery = useQuery({ queryKey: ['insured', 'assigned-to-me'], queryFn: () => profileApi.listAssignedInsured({ page: 0, size: 50 }), enabled: isDoctor });
  const query = isDoctor ? doctorQuery : agentQuery;
  const setFilter = (key: string, value: string) => { const next = new URLSearchParams(params); if (value) next.set(key, value); else next.delete(key); setParams(next, { replace: true }); };

  return <div>
    <PageHeader title={isDoctor ? 'Mes patients' : 'Patients couverts'} description={isDoctor ? 'Patients pour lesquels vous êtes médecin traitant.' : 'Liste filtrable des patients assurés.'} action={<RoleGate roles={['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'ADMIN']}><Link to="/app/insured/new"><Button icon={<UserPlus className="h-4 w-4" />}>Nouveau</Button></Link></RoleGate>} />
    {!isDoctor && <div className="mb-4 grid gap-3 sm:grid-cols-2"><Input label="Rechercher" value={search} onChange={(event) => setFilter('search', event.target.value)} icon={<Search className="h-4 w-4" />} /><Select label="Statut" value={status} onChange={(event) => setFilter('status', event.target.value)} options={[{ label: 'Tous', value: '' }, { label: 'Actifs', value: 'ACTIVE' }, { label: 'Inactifs', value: 'SUSPENDED' }]} /></div>}
    {query.isLoading && <Loader />}{query.isError && <ErrorState message={extractApiError(query.error)} />}
    {query.data && <DataTable headers={['Identifiant', 'Patient', 'Statut', 'Contact', 'Médecin traitant']} empty="Aucun patient trouvé.">{query.data.content.map((patient) => <tr key={patient.id} className="care-table-row"><td className="care-table-cell-strong px-4 py-3"><Link className="text-primary-700 hover:underline" to={`/app/insured/${patient.insuranceNumber}`}>{patient.insuranceNumber}</Link></td><td className="care-table-cell px-4 py-3">{patient.firstName} {patient.lastName}</td><td className="px-4 py-3"><Badge tone={patient.status === 'ACTIVE' ? 'success' : 'warning'}>{patient.status === 'ACTIVE' ? 'Actif' : 'Inactif'}</Badge></td><td className="care-table-cell px-4 py-3">{patient.email ?? patient.phoneNumber ?? '-'}</td><td className="care-table-cell px-4 py-3">{patient.treatingDoctor ? `Dr ${patient.treatingDoctor.lastName}` : '-'}</td></tr>)}</DataTable>}
  </div>;
}
