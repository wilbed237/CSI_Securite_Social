import { useQuery } from '@tanstack/react-query';
import { Stethoscope } from 'lucide-react';
import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useParams } from 'react-router-dom';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { RoleGate } from '../../components/RoleGate';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { ErrorState } from '../../components/ui/ErrorState';
import { Input } from '../../components/ui/Input';
import { Loader } from '../../components/ui/Loader';
import { usePageTitle } from '../../hooks/usePageTitle';

export function InsuredDetailPage() {
  const { insuranceNumber = '' } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  usePageTitle(`Patient couvert ${insuranceNumber}`);
  const [doctorMatricule, setDoctorMatricule] = useState('MED-GEN-001');
  const query = useQuery({ queryKey: ['insured', insuranceNumber], queryFn: () => profileApi.getInsured(insuranceNumber) });
  const history = useQuery({ queryKey: ['primary-doctor-history', insuranceNumber], queryFn: () => profileApi.primaryDoctorHistory(insuranceNumber) });

  const assign = async () => {
    try { await profileApi.assignTreatingDoctor(insuranceNumber, doctorMatricule); toast.success('Praticien référent associé'); query.refetch(); }
    catch (error) { toast.error(extractApiError(error)); }
  };

  if (query.isLoading) return <Loader />;
  if (query.isError) return <ErrorState message={extractApiError(query.error)} />;
  const insured = query.data!;
  return (
    <div>
      <PageHeader title={`${insured.firstName} ${insured.lastName}`} description={`Dossier patient couvert ${insured.insuranceNumber}`} />
      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        {location.state?.showTreatingDoctorHint && <div className="xl:col-span-2 rounded-2xl border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">Le médecin traitant peut être assigné à tout moment depuis cette fiche. Si nécessaire, vous pouvez le faire maintenant depuis le bloc ci-dessous.</div>}
        <Card>
          <CardHeader title="Informations administratives" />
          <dl className="grid gap-4 sm:grid-cols-2">
            <Info label="Statut" value={<Badge tone={insured.status === 'ACTIVE' ? 'success' : 'warning'}>{insured.status}</Badge>} />
            <Info label="Naissance" value={insured.birthDate} />
            <Info label="Adresse" value={insured.address} />
            <Info label="Téléphone" value={insured.phoneNumber ?? '-'} />
            <Info label="Email" value={insured.email ?? '-'} />
            <Info label="Pays" value={insured.countryCode ?? 'CM'} />
            <Info label="Mode remboursement" value={insured.preferredPaymentType ?? 'CASH'} />
            <Info label="Compte bancaire" value={insured.bankAccountMasked ?? '-'} />
          </dl>
        </Card>
        <Card>
          <CardHeader title="Praticien référent" description="Le référentiel clinique impose un généraliste comme médecin de référence." />
          {insured.treatingDoctor ? <div className="rounded-xl bg-secondary-50 p-4"><p className="font-bold text-slate-950">Dr {insured.treatingDoctor.firstName} {insured.treatingDoctor.lastName}</p><p className="text-sm text-slate-500">{insured.treatingDoctor.matricule} · {insured.treatingDoctor.type}</p><p className="mt-2 text-xs text-slate-500">Le médecin traitant peut être remplacé à tout moment en saisissant un autre matricule généraliste.</p></div> : <div className="rounded-xl border border-amber-200 bg-amber-50 p-4"><p className="text-sm font-semibold text-amber-900">Aucun praticien référent enregistré.</p><p className="mt-1 text-sm text-amber-700">Vous pouvez l’assigner à tout moment depuis cette page.</p></div>}
          <RoleGate roles={['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'ADMIN']}>
            <div className="mt-5 flex flex-col gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 sm:flex-row sm:items-end">
              <Input label="Matricule généraliste" value={doctorMatricule} onChange={(e) => setDoctorMatricule(e.target.value)} />
              <Button icon={<Stethoscope className="h-4 w-4" />} onClick={assign}>Assigner un médecin traitant</Button>
            </div>
          </RoleGate>
          <div className="mt-4 flex flex-wrap gap-3">
            <Button variant="ghost" onClick={() => navigate('/app/insured')}>Retour à la liste</Button>
            <Button variant="secondary" onClick={() => navigate(`/app/insured/${insuranceNumber}`)}>Actualiser la fiche</Button>
          </div>
          {history.data && history.data.length > 0 && <div className="mt-6 border-t border-slate-200 pt-4"><p className="text-sm font-semibold">Historique</p>{history.data.map((item) => <p key={item.id} className="mt-2 text-xs text-slate-500">Dr {item.doctor.firstName} {item.doctor.lastName} · depuis {new Date(item.startedAt).toLocaleDateString()}</p>)}</div>}
        </Card>
      </div>
    </div>
  );
}

function Info({ label, value }: { label: string; value: React.ReactNode }) {
  return <div><dt className="text-xs font-semibold uppercase tracking-wide text-slate-400">{label}</dt><dd className="mt-1 text-sm font-medium text-slate-800">{value}</dd></div>;
}
