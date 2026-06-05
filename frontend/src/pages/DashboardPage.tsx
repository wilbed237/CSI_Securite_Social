import { Activity, ClipboardList, Stethoscope, WalletCards } from 'lucide-react';
import { Link } from 'react-router-dom';
import { PageHeader } from '../components/PageHeader';
import { RoleGate } from '../components/RoleGate';
import { Button } from '../components/ui/Button';
import { Card, CardHeader } from '../components/ui/Card';
import { StatCard } from '../components/ui/StatCard';
import { usePageTitle } from '../hooks/usePageTitle';
import { useAuthStore } from '../store/authStore';

export function DashboardPage() {
  usePageTitle('Tableau de bord médical');
  const user = useAuthStore((state) => state.user);
  return (
    <div>
      <PageHeader title={`Bonjour ${user?.username ?? ''}`} description="Vue d'ensemble des modules cliniques disponibles selon votre rôle." />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard title="Patients couverts" value="ASS-0001" description="Donnée de test disponible dans module dossiers patients." icon={<Activity className="h-6 w-6" />} />
        <StatCard title="Médecins" value="2" description="Généraliste et spécialiste de démonstration." icon={<Stethoscope className="h-6 w-6" />} />
        <StatCard title="Feuilles" value="FM" description="Créer après une consultation validée." icon={<ClipboardList className="h-6 w-6" />} />
        <StatCard title="Prise en charge" value="100/80%" description="Calcul selon le parcours de soins." icon={<WalletCards className="h-6 w-6" />} />
      </div>
      <Card className="mt-6">
        <CardHeader title="Actions rapides" description="Accès direct aux gestes métier les plus fréquents." />
        <div className="flex flex-wrap gap-3">
          <RoleGate roles={['AGENT']}><Link to="/app/insured/new"><Button>Inscrire un patient couvert</Button></Link></RoleGate>
          <RoleGate roles={['DOCTOR', 'GENERALIST', 'SPECIALIST']}><Link to="/app/consultations/new"><Button>Créer la consultation</Button></Link></RoleGate>
          <RoleGate roles={['DOCTOR', 'GENERALIST', 'SPECIALIST']}><Link to="/app/ordonnances"><Button variant="secondary">Prescrire</Button></Link></RoleGate>
          <RoleGate roles={['AGENT']}><Link to="/app/reimbursements"><Button variant="secondary">Valider une prise en charge</Button></Link></RoleGate>
        </div>
      </Card>
    </div>
  );
}
