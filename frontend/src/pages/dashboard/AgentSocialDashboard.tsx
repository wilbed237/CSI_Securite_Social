import { useQuery } from '@tanstack/react-query';
import { Activity, CircleDollarSign, RefreshCw, Stethoscope, Users } from 'lucide-react';
import { useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { dashboardApi } from '../../api/dashboardApi';
import { extractApiError } from '../../api/httpClient';
import { settingsApi } from '../../api/settingsApi';
import { ChartCard } from '../../components/dashboard/ChartCard';
import { ClickableStatCard } from '../../components/dashboard/ClickableStatCard';
import { DashboardFilters } from '../../components/dashboard/DashboardFilters';
import { RecentActivityList } from '../../components/dashboard/RecentActivityList';
import { Button } from '../../components/ui/Button';
import { ErrorState } from '../../components/ui/ErrorState';
import { Loader } from '../../components/ui/Loader';
import type { DashboardFilters as Filters, DashboardPeriod } from '../../types/dashboard';
import type { DoctorType, ReimbursementStatus, ReimbursementType } from '../../types/api';

const money = new Intl.NumberFormat('fr-CM', { style: 'currency', currency: 'XAF', maximumFractionDigits: 0 });
const defaults: Filters = { period: 'MONTH' };

function readFilters(params: URLSearchParams): Filters {
  return {
    period: (params.get('period') as DashboardPeriod) || 'MONTH',
    startDate: params.get('startDate') || undefined,
    endDate: params.get('endDate') || undefined,
    reimbursementType: (params.get('type') as ReimbursementType) || undefined,
    reimbursementStatus: (params.get('status') as ReimbursementStatus) || undefined,
    doctorType: (params.get('doctorType') as DoctorType) || undefined,
    specialty: params.get('specialty') || undefined,
  };
}

function toParams(filters: Filters, extras: Record<string, string> = {}) {
  const params = new URLSearchParams({ period: filters.period, ...extras });
  if (filters.startDate) params.set('startDate', filters.startDate);
  if (filters.endDate) params.set('endDate', filters.endDate);
  if (filters.reimbursementType) params.set('type', filters.reimbursementType);
  if (filters.reimbursementStatus) params.set('status', filters.reimbursementStatus);
  if (filters.doctorType) params.set('doctorType', filters.doctorType);
  if (filters.specialty) params.set('specialty', filters.specialty);
  return params.toString();
}

export function AgentSocialDashboard() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const filters = useMemo(() => readFilters(searchParams), [searchParams]);
  const summary = useQuery({ queryKey: ['dashboard', 'agent-summary', filters], queryFn: () => dashboardApi.getAgentSummary(filters), refetchInterval: 60_000 });
  const reimbursements = useQuery({ queryKey: ['dashboard', 'agent-reimbursements', filters], queryFn: () => dashboardApi.getAgentReimbursementsStats(filters), refetchInterval: 60_000 });
  const recent = useQuery({ queryKey: ['dashboard', 'agent-recent'], queryFn: dashboardApi.getAgentRecentActivities });
  const consulted = useQuery({ queryKey: ['dashboard', 'doctors-consulted', filters], queryFn: () => dashboardApi.getDoctorsConsulted(filters), refetchInterval: 60_000 });
  const settings = useQuery({ queryKey: ['settings'], queryFn: settingsApi.listAll, staleTime: 300_000 });

  const updateFilters = (next: Filters) => setSearchParams(toParams(next), { replace: true });
  const refresh = () => { void summary.refetch(); void reimbursements.refetch(); void recent.refetch(); };
  if (summary.isLoading || reimbursements.isLoading) return <Loader />;
  if (summary.isError) return <ErrorState message={extractApiError(summary.error)} />;
  if (reimbursements.isError) return <ErrorState message={extractApiError(reimbursements.error)} />;
  if (!summary.data || !reimbursements.data) return <ErrorState message="Aucune donnée de tableau de bord disponible." />;

  const s = summary.data;
  const r = reimbursements.data;
  const settingsList = settings.data?.flatMap((category) => category.settings ?? []) ?? [];
  const reimbursementUrl = (extras: Record<string, string> = {}) => `/app/reimbursements?${toParams(filters, extras)}`;
  const updatedAt = new Intl.DateTimeFormat('fr-CM', { timeStyle: 'short' }).format(new Date());

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-slate-500">Dernière actualisation : {updatedAt}</p>
        <Button variant="secondary" icon={<RefreshCw className="h-4 w-4" />} onClick={refresh} isLoading={summary.isFetching || reimbursements.isFetching}>Actualiser</Button>
      </div>
      <DashboardFilters filters={filters} onChange={updateFilters} onReset={() => updateFilters(defaults)} settings={settingsList} />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <ClickableStatCard title="Patients assurés" value={String(s.totalInsuredPatients ?? 0)} description={`${s.activeInsuredPatients ?? 0} actifs, ${s.newInsuredPatientsInPeriod ?? s.newInsuredPatientsThisMonth ?? 0} nouveaux sur la période`} icon={<Users className="h-6 w-6" />} to={`/app/insured?status=ACTIVE&period=${filters.period}`} />
        <ClickableStatCard title="Médecins en service" value={String(s.activeDoctors)} description={`${s.generalistDoctors} généralistes, ${s.specialistDoctors} spécialistes`} icon={<Stethoscope className="h-6 w-6" />} to={`/app/doctors?active=true&${toParams(filters)}`} />
        <ClickableStatCard title="Remboursements" value={String(r.totalReimbursements)} description={`${r.pendingReimbursements} en attente, ${r.rejectedReimbursements} rejetés`} icon={<Activity className="h-6 w-6" />} to={reimbursementUrl()} />
        <ClickableStatCard title="Montant remboursé" value={money.format(r.totalReimbursedAmount ?? 0)} description={`${r.currentAgentProcessedCount ?? 0} dossiers traités par vous, moyenne ${money.format(r.averageReimbursedAmount ?? 0)}`} icon={<CircleDollarSign className="h-6 w-6" />} to={reimbursementUrl({ status: 'EXECUTED', processedByCurrentUser: 'true' })} />
      </div>
      <div className="grid gap-4 xl:grid-cols-2">
        <ChartCard title="Évolution mensuelle des patients" points={(s.insuredPatientsMonthlyEvolution ?? []).map((point) => ({ label: point.month ?? '', value: point.count }))} onPointClick={(point) => navigate(`/app/insured?month=${point.label}`)} />
        <ChartCard title="Remboursements par mois" points={(r.monthlyEvolution ?? []).map((point) => ({ label: point.month ?? '', value: point.count }))} onPointClick={(point) => navigate(reimbursementUrl({ month: point.label }))} />
        <ChartCard title="Remboursements par statut" points={(r.byStatus ?? []).map((point) => ({ label: point.status, value: point.count }))} onPointClick={(point) => navigate(reimbursementUrl({ status: point.label }))} />
        <ChartCard title="Médecins par type" points={(s.doctorsByType ?? []).map((point) => ({ label: point.type, value: point.count }))} onPointClick={(point) => navigate(`/app/doctors?type=${point.label}&active=true`)} />
        <ChartCard title="Consultations généralistes / spécialistes" points={(consulted.data?.byType ?? []).map((point) => ({ label: point.type, value: point.count }))} onPointClick={(point) => navigate(`/app/doctors?type=${point.label}&active=true`)} />
        <ChartCard title="Remboursements par prestation" points={(r.byType ?? []).map((point) => ({ label: point.type, value: point.count }))} onPointClick={(point) => navigate(reimbursementUrl({ type: point.label }))} />
        <ChartCard title="Montant remboursé par mois" points={(r.amountByMonth ?? []).map((point) => ({ label: point.month, value: point.amount }))} onPointClick={(point) => navigate(reimbursementUrl({ month: point.label }))} />
      </div>
      <RecentActivityList title="Médecins récemment ajoutés" items={(recent.data?.recentlyActiveDoctors ?? []).map((doctor) => ({ id: doctor.id, title: `Dr ${doctor.firstName} ${doctor.lastName}`, description: doctor.specialty ?? doctor.type, meta: doctor.matricule }))} />
    </div>
  );
}
