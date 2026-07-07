import { ArrowRight, ShieldCheck, Stethoscope, WalletCards } from 'lucide-react';
import { Link, Navigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { ThemeToggle } from '../components/ThemeToggle';
import { CareHealthLogo } from '../components/brand/CareHealthLogo';
import { useAuthStore } from '../store/authStore';
import { usePageTitle } from '../hooks/usePageTitle';
import { useTranslation } from '../i18n';

export function LandingPage() {
  usePageTitle('Accueil');
  const { t } = useTranslation();
  const token = useAuthStore((state) => state.accessToken);
  if (token) return <Navigate to="/app" replace />;

  return (
    <main className="relative min-h-screen overflow-hidden bg-slate-50">
      <div className="absolute right-4 top-4 z-10 sm:right-8 sm:top-8">
        <ThemeToggle />
      </div>
      <section className="gradient-health flex min-h-screen items-center px-6 py-20 text-white sm:py-24">
        <div className="mx-auto grid max-w-6xl gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <div>
            <div className="inline-flex rounded-3xl bg-white/10 px-4 py-3 ring-1 ring-white/20"><CareHealthLogo className="text-white" /></div>
            <h1 className="landing-title mt-6 max-w-4xl text-4xl font-black leading-[1.05] tracking-tight sm:text-6xl xl:text-7xl">
              {t('landing.headlineFirst', 'Un cockpit clinique')}
              <span className="block pt-2 text-secondary-100">{t('landing.headlineSecond', 'pour vos parcours de soins.')}</span>
            </h1>
            <p className="mt-6 max-w-2xl text-lg leading-8 text-blue-50">Pilotez les dossiers patients, les praticiens, les ordonnances, les feuilles de soins et les prises en charge depuis une expérience médicale claire, sécurisée et responsive.</p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/login"><Button icon={<ArrowRight className="h-4 w-4" />}>Se connecter</Button></Link>
            </div>
          </div>
          <div className="rounded-3xl bg-white/10 p-4 ring-1 ring-white/20 sm:p-6">
            <div className="grid gap-4">
              {[['Patients suivis', 'Validation clinique avant tout acte de soin', ShieldCheck], ['Actes cliniques', 'Consultations, ordonnances et feuilles de soins', Stethoscope], ['Prise en charge', 'Calcul automatisé selon le parcours thérapeutique', WalletCards]].map(([title, desc, Icon]) => (
                <div key={String(title)} className="rounded-2xl bg-white p-5 text-slate-900 shadow-lg">
                  <div className="flex gap-4"><span className="rounded-2xl bg-secondary-100 p-3 text-primary-700"><Icon className="h-6 w-6" /></span><div><h3 className="font-bold">{title as string}</h3><p className="mt-1 text-sm text-slate-500">{desc as string}</p></div></div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
