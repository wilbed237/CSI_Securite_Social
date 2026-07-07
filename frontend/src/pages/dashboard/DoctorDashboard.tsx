import { useQuery } from '@tanstack/react-query';
import { ClipboardPlus, FileText, Pill, Search, Stethoscope, UserCheck } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { dashboardApi } from '../../api/dashboardApi';
import { extractApiError } from '../../api/httpClient';
import { ChartCard } from '../../components/dashboard/ChartCard';
import { StatCard } from '../../components/dashboard/StatCard';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { ErrorState } from '../../components/ui/ErrorState';
import { Loader } from '../../components/ui/Loader';

export function DoctorDashboard() {
  const summary = useQuery({ queryKey: ['dashboard', 'doctor-summary'], queryFn: () => dashboardApi.getDoctorSummary() });
  const canCreateConsultation = useAuthStore((state) => state.hasAnyRole(['DOCTOR', 'GENERALIST', 'SPECIALIST']));
  const canCreateDiseaseSheet = useAuthStore((state) => state.hasAnyRole(['DOCTOR', 'GENERALIST', 'SPECIALIST']));
  const canCreatePrescription = useAuthStore((state) => state.hasAnyRole(['DOCTOR', 'GENERALIST', 'SPECIALIST']));

  if (summary.isLoading) return <Loader />;
  if (summary.isError) return <ErrorState message={extractApiError(summary.error)} />;
  if (!summary.data) return null;

  const data = summary.data;
  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Patients aujourd'hui" value={String(data.patientsToday)} description={`${data.patientsThisWeek} cette semaine, ${data.patientsThisMonth} ce mois`} icon={<UserCheck className="h-6 w-6" />} />
        <StatCard title="Consultations" value={String(data.consultations)} description={`${data.pendingConsultations} en attente`} icon={<Stethoscope className="h-6 w-6" />} />
        <StatCard title="Feuilles maladie" value={String(data.diseaseSheets)} description="Feuilles produites pour remboursement" icon={<FileText className="h-6 w-6" />} />
        <StatCard title="Recommandations" value={String(data.specialistRecommendations)} description={`${data.medicationPrescriptions} prescriptions médicament`} icon={<Pill className="h-6 w-6" />} />
      </div>
      <Card>
        <CardHeader title="Actions rapides" />
        <div className="flex flex-wrap gap-3">
          {canCreateConsultation ? <Link to="/app/consultations/new"><Button icon={<ClipboardPlus className="h-4 w-4" />}>Créer une consultation</Button></Link> : null}
          {canCreateDiseaseSheet ? <Link to="/app/disease-sheets"><Button variant="secondary" icon={<FileText className="h-4 w-4" />}>Créer une feuille maladie</Button></Link> : null}
          {canCreatePrescription ? <Link to="/app/ordonnances"><Button variant="secondary" icon={<Pill className="h-4 w-4" />}>Rédiger une prescription</Button></Link> : null}
          <Link to="/app/insured"><Button variant="ghost" icon={<Search className="h-4 w-4" />}>Rechercher un patient</Button></Link>
        </div>
      </Card>
      <div className="grid gap-4 xl:grid-cols-2">
        <ChartCard title="Consultations par jour" points={data.consultationsByDay.map((point) => ({ label: point.label ?? '', value: point.count }))} />
        <ChartCard title="Feuilles maladie par mois" points={data.diseaseSheetsByMonth.map((point) => ({ label: point.label ?? '', value: point.count }))} />
        <ChartCard title="Prescriptions par catégorie" points={data.prescriptionsByCategory.map((point) => ({ label: point.category, value: point.count }))} />
      </div>
    </div>
  );
}
