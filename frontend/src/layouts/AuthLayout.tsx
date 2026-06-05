import { Outlet } from 'react-router-dom';
import { env } from '../config/env';
import { ThemeToggle } from '../components/ThemeToggle';
import { CareHealthLogo } from '../components/brand/CareHealthLogo';
import { PageTransition } from '../components/PageTransition';

export function AuthLayout() {
  const appName = String(env.appName);

  return (
    <main className="relative grid min-h-screen bg-slate-50 lg:grid-cols-[1.05fr_0.95fr]">
      <div className="absolute right-4 top-4 z-10 sm:right-8 sm:top-8">
        <ThemeToggle className="border-slate-200 bg-white text-slate-700 hover:bg-slate-100 lg:border-white/20 lg:bg-white/10 lg:text-white lg:hover:bg-white/20" />
      </div>
      <section className="gradient-health hidden items-center justify-center p-10 text-white lg:flex">
        <div className="max-w-xl">
          <div className="animated-wave-logo mb-8 inline-flex rounded-2xl bg-white/10 px-5 py-2.5 ring-1 ring-white/20" aria-label={appName}>
            <CareHealthLogo className="text-white" />
          </div>
          <h1 className="text-5xl font-black leading-tight">Orchestration clinique des patients, praticiens et parcours de soins.</h1>
          <p className="mt-6 text-lg leading-8 text-blue-50">Une console médicale fluide pour suivre les dossiers patients, les actes cliniques et la continuité thérapeutique.</p>
        </div>
      </section>
      <section className="flex items-center justify-center p-6">
        <PageTransition>
          <Outlet />
        </PageTransition>
      </section>
    </main>
  );
}
