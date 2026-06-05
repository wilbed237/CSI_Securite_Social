import { LogOut, Menu, ShieldCheck, X } from 'lucide-react';
import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { navigationItems } from '../routes/navigation';
import { useAuthStore } from '../store/authStore';
import { Button } from '../components/ui/Button';
import { Badge } from '../components/ui/Badge';
import { ThemeToggle } from '../components/ThemeToggle';
import { cn } from '../utils/cn';

export function AppLayout() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const { user, clearSession, hasAnyRole } = useAuthStore();
  const visibleItems = navigationItems.filter((item) => !item.roles || hasAnyRole(item.roles));

  const logout = () => {
    clearSession();
    navigate('/login', { replace: true });
  };

  return (
    <div className="min-h-screen bg-slate-50 lg:grid lg:grid-cols-[280px_1fr]">
      <aside className={cn('fixed inset-y-0 left-0 z-40 w-72 -translate-x-full border-r border-slate-200 bg-white transition lg:static lg:translate-x-0', open && 'translate-x-0')}>
        <div className="flex h-full flex-col p-5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <span className="rounded-2xl bg-primary-700 p-2 text-white"><ShieldCheck className="h-6 w-6" /></span>
              <div>
                <p className="font-black text-slate-950">CSI Santé</p>
                <p className="text-xs text-slate-500">Back-office médical</p>
              </div>
            </div>
            <button className="lg:hidden" onClick={() => setOpen(false)} aria-label="Fermer le menu"><X /></button>
          </div>
          <nav className="mt-8 space-y-1">
            {visibleItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/app'}
                onClick={() => setOpen(false)}
                className={({ isActive }) => cn(
                  'flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold text-slate-600 transition hover:bg-slate-100 hover:text-slate-950',
                  isActive && 'bg-primary-50 text-primary-700',
                )}
              >
                <item.icon className="h-5 w-5" />
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="mt-auto rounded-2xl bg-slate-50 p-4">
            <p className="text-sm font-bold text-slate-950">{user?.username}</p>
            <p className="truncate text-xs text-slate-500">{user?.email}</p>
            <div className="mt-3 flex flex-wrap gap-1.5">{user?.roles.map((role) => <Badge key={role} tone="primary">{role}</Badge>)}</div>
            <Button className="mt-4 w-full" variant="ghost" icon={<LogOut className="h-4 w-4" />} onClick={logout}>Déconnexion</Button>
          </div>
        </div>
      </aside>
      {open && <div className="fixed inset-0 z-30 bg-slate-950/40 lg:hidden" onClick={() => setOpen(false)} />}
      <div className="min-w-0">
        <header className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 px-4 py-3 backdrop-blur lg:px-8">
          <div className="flex items-center justify-between gap-4">
            <button className="rounded-xl border border-slate-200 p-2 lg:hidden" onClick={() => setOpen(true)} aria-label="Ouvrir le menu"><Menu className="h-5 w-5" /></button>
            <div>
              <p className="text-sm text-slate-500">Application CSI</p>
              <h1 className="text-xl font-black text-slate-950">Espace sécurisé</h1>
            </div>
            <div className="flex items-center gap-2">
              <ThemeToggle className="border-slate-200 bg-white text-slate-700 hover:bg-slate-100" />
              <Badge tone="success">JWT actif</Badge>
            </div>
          </div>
        </header>
        <main className="p-4 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
