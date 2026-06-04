import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button';

export function ForbiddenPage() {
  return <main className="flex min-h-screen items-center justify-center bg-slate-50 p-6"><div className="max-w-md text-center"><h1 className="text-4xl font-black text-slate-950">Accès refusé</h1><p className="mt-3 text-slate-600">Votre rôle ne permet pas d'accéder à cette fonctionnalité.</p><Link to="/app"><Button className="mt-6">Retour dashboard</Button></Link></div></main>;
}
