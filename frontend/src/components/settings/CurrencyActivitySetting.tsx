import { CircleDollarSign, ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { ReimbursementStats } from '../../types/dashboard';
import { Button } from '../ui/Button';

const money = new Intl.NumberFormat('fr-CM', { style: 'currency', currency: 'XAF', maximumFractionDigits: 0 });
export function CurrencyActivitySetting({ stats }: { stats?: ReimbursementStats }) {
  return <div className="rounded-lg border border-slate-200 p-4 transition hover:border-primary-300"><div className="flex items-center gap-3"><CircleDollarSign className="h-5 w-5 text-primary-700" /><div><h3 className="font-bold text-slate-950">Devise et activité financière</h3><p className="text-sm text-slate-500">Franc CFA · XAF · FCFA</p></div></div><div className="mt-4 grid gap-2 text-sm sm:grid-cols-3"><p><strong>Montant traité :</strong><br />{money.format(stats?.currentAgentReimbursedAmount ?? 0)}</p><p><strong>Dossiers exécutés :</strong><br />{stats?.currentAgentProcessedCount ?? 0}</p><p><strong>Montant moyen :</strong><br />{money.format(stats?.averageReimbursedAmount ?? 0)}</p></div><Link className="mt-4 inline-block" to="/app/reimbursements?period=MONTH&status=EXECUTED&processedByCurrentUser=true"><Button variant="secondary" icon={<ExternalLink className="h-4 w-4" />}>Voir mes remboursements</Button></Link></div>;
}
