import { Outlet } from 'react-router-dom';
import { env } from '../config/env';

export function AuthLayout() {
  const appName = String(env.appName);

  return (
    <main className="grid min-h-screen bg-slate-50 lg:grid-cols-[1.05fr_0.95fr]">
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
