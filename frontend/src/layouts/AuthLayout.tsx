import { Outlet } from 'react-router-dom';
import { env } from '../config/env';
import { ThemeToggle } from '../components/ThemeToggle';

export function AuthLayout() {
  const appName = String(env.appName);

  return (
    <main className="relative grid min-h-screen bg-slate-50 lg:grid-cols-[1.05fr_0.95fr]">
      <div className="absolute right-4 top-4 z-10 sm:right-8 sm:top-8">
        <ThemeToggle className="border-slate-200 bg-white text-slate-700 hover:bg-slate-100 lg:border-white/20 lg:bg-white/10 lg:text-white lg:hover:bg-white/20" />
      </div>
      <section className="gradient-health hidden items-center justify-center p-10 text-white lg:flex">
        <div className="max-w-xl">
          <div className="animated-wave-logo mb-8 inline-flex rounded-2xl bg-white/10 px-5 py-2.5 text-base font-black tracking-wide ring-1 ring-white/20" aria-label={appName}>
            {appName.split('').map((letter: string, index: number) => (
              <span key={`${letter}-${index}`} style={{ animationDelay: `${index * 0.055}s` }}>
                {letter === ' ' ? '\u00A0' : letter}
              </span>
            ))}
          </div>
          <h1 className="text-5xl font-black leading-tight">Gestion digitale des assurés, médecins et remboursements.</h1>
          <p className="mt-6 text-lg leading-8 text-blue-50">Une interface moderne pour les agents de sécurité sociale et les médecins, alignée sur le cahier de charges CSI.</p>
        </div>
      </section>
      <section className="flex items-center justify-center p-6">
        <Outlet />
      </section>
    </main>
  );
}
