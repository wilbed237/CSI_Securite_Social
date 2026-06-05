import { ArrowRight, ShieldCheck, Stethoscope, WalletCards } from 'lucide-react';
import { Link, Navigate } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { ThemeToggle } from '../components/ThemeToggle';
import { useAuthStore } from '../store/authStore';
import { usePageTitle } from '../hooks/usePageTitle';

export function LandingPage() {
  usePageTitle('Accueil');
  const token = useAuthStore((state) => state.accessToken);
  if (token) return <Navigate to="/app" replace />;

  return (
    <main className="relative min-h-screen bg-slate-50">
      <div className="absolute right-4 top-4 z-10 sm:right-8 sm:top-8">
        <ThemeToggle />
      </div>
      <section className="gradient-health px-6 py-16 text-white sm:py-24">
        <div className="mx-auto grid max-w-6xl gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <div>
            <span className="inline-flex rounded-full bg-white/10 px-4 py-2 text-sm font-semibold ring-1 ring-white/20">CSI Assurance Santé</span>
            <h1 className="animated-wave-title mt-6 text-5xl font-black leading-tight tracking-tight sm:text-7xl xl:text-8xl" aria-label="Une plateforme moderne pour la sécurité sociale médicale.">
              {'Une plateforme moderne'.split('').map((letter, index) => (
                <span key={`${letter}-${index}`} style={{ animationDelay: `${index * 0.045}s` }}>
                  {letter === ' ' ? '\u00A0' : letter}
                </span>
              ))}
              <span className="block pt-2 text-secondary-100">
                {'pour la sécurité sociale médicale.'.split('').map((letter, index) => (
                  <span key={`${letter}-${index}`} style={{ animationDelay: `${(index + 24) * 0.045}s` }}>
                    {letter === ' ' ? '\u00A0' : letter}
                  </span>
                ))}
              </span>
            </h1>
            <p className="mt-6 max-w-2xl text-lg leading-8 text-blue-50">Gérez les assurés, médecins, prescriptions, feuilles de maladie et remboursements depuis une interface claire, sécurisée et responsive.</p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/login"><Button icon={<ArrowRight className="h-4 w-4" />}>Se connecter</Button></Link>
              <Link to="/register"><Button variant="secondary">Créer un compte</Button></Link>
            </div>
          </div>
          <div className="rounded-3xl bg-white/10 p-6 ring-1 ring-white/20 backdrop-blur">
            <div className="grid gap-4">
              {[['Assurés actifs', 'Vérification rapide avant acte médical', ShieldCheck], ['Actes médicaux', 'Consultations, prescriptions et feuilles maladie', Stethoscope], ['Remboursements', 'Calcul automatique 100% / 80%', WalletCards]].map(([title, desc, Icon]) => (
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
