import { AlertTriangle, RefreshCw } from 'lucide-react';
import { Link, isRouteErrorResponse, useRouteError } from 'react-router-dom';
import { Button } from '../../components/ui/Button';

export function RouteErrorPage() {
  const error = useRouteError();
  const message = isRouteErrorResponse(error)
    ? error.statusText || 'La page demandée ne peut pas être affichée.'
    : error instanceof Error ? error.message : 'Une erreur inattendue est survenue.';
  return <main className="flex min-h-[60vh] items-center justify-center p-6"><div className="max-w-lg text-center"><AlertTriangle className="mx-auto h-10 w-10 text-red-600" /><h1 className="mt-4 text-2xl font-black text-slate-950">Impossible d’afficher cette page</h1><p className="mt-2 text-slate-600">{message}</p><div className="mt-6 flex justify-center gap-3"><Button icon={<RefreshCw className="h-4 w-4" />} onClick={() => window.location.reload()}>Réessayer</Button><Link to="/app"><Button variant="secondary">Tableau de bord</Button></Link></div></div></main>;
}
