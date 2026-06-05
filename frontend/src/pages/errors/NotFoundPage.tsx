import { Link } from 'react-router-dom';
import { Button } from '../../components/ui/Button';

export function NotFoundPage() {
  return <main className="flex min-h-screen items-center justify-center bg-slate-50 p-6"><div className="max-w-md text-center"><h1 className="text-4xl font-black text-slate-950">Page introuvable</h1><p className="mt-3 text-slate-600">La route demandée n'existe pas dans l'espace clinique Care Health.</p><Link to="/"><Button className="mt-6">Retour accueil</Button></Link></div></main>;
}
