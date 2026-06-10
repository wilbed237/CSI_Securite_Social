import { useMemo, useState } from 'react';
import { PageHeader } from '../components/PageHeader';
import { Button } from '../components/ui/Button';
import { usePageTitle } from '../hooks/usePageTitle';
import { useAuthStore } from '../store/authStore';
import { AgentSocialDashboard } from './dashboard/AgentSocialDashboard';
import { DoctorDashboard } from './dashboard/DoctorDashboard';

type DashboardView = 'agent' | 'doctor';

export function DashboardPage() {
  usePageTitle('Tableau de bord');
  const user = useAuthStore((state) => state.user);
  const roles = user?.roles ?? [];
  const canSeeAgent = roles.some((role) => ['AGENT', 'ADMIN', 'SOCIAL_AGENT', 'AGENT_SOCIAL', 'SECURITY_AGENT'].includes(role));
  const canSeeDoctor = roles.some((role) => ['DOCTOR', 'GENERALIST', 'SPECIALIST'].includes(role));
  const defaultView = useMemo<DashboardView>(() => (canSeeAgent ? 'agent' : 'doctor'), [canSeeAgent]);
  const [view, setView] = useState<DashboardView>(defaultView);
  const activeView = view === 'agent' && canSeeAgent ? 'agent' : canSeeDoctor ? 'doctor' : 'agent';

  return (
    <div>
      <PageHeader
        title={`Bonjour ${user?.username ?? ''}`}
        description="Vue d'ensemble des indicateurs opérationnels selon votre rôle."
        action={canSeeAgent && canSeeDoctor ? (
          <div className="flex rounded-xl border border-slate-200 bg-white p-1">
            <Button variant={activeView === 'agent' ? 'primary' : 'ghost'} onClick={() => setView('agent')}>Agent</Button>
            <Button variant={activeView === 'doctor' ? 'primary' : 'ghost'} onClick={() => setView('doctor')}>Médecin</Button>
          </div>
        ) : undefined}
      />
      {activeView === 'agent' && canSeeAgent ? <AgentSocialDashboard /> : <DoctorDashboard />}
    </div>
  );
}
