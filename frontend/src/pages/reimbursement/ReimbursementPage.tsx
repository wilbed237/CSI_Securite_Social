import { zodResolver } from '@hookform/resolvers/zod';
import { CreditCard, Search } from 'lucide-react';
import { useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import toast from 'react-hot-toast';
import { z } from 'zod';
import { reimbursementApi } from '../../api/reimbursementApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { ConfirmModal } from '../../components/ui/ConfirmModal';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { useDisclosure } from '../../hooks/useDisclosure';
import { usePageTitle } from '../../hooks/usePageTitle';
import type { ReimbursementResponse } from '../../types/api';

const schema = z.object({ sheetNumber: z.string().min(1), paymentType: z.enum(['CASH', 'BANK_TRANSFER']), bankIban: z.string().optional() }).superRefine((value, ctx) => {
  if (value.paymentType === 'BANK_TRANSFER' && !value.bankIban) ctx.addIssue({ code: 'custom', path: ['bankIban'], message: 'IBAN requis pour un virement' });
});
type FormValues = z.infer<typeof schema>;

export function ReimbursementPage() {
  usePageTitle('Prises en charge');
  const [last, setLast] = useState<ReimbursementResponse | null>(null);
  const [reference, setReference] = useState('');
  const modal = useDisclosure();
  const form = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { paymentType: 'CASH' } });
  const paymentType = useWatch({ control: form.control, name: 'paymentType' });

  const submit = async (values: FormValues) => {
    try { const result = await reimbursementApi.create(values); setLast(result); toast.success('Prise en charge exécuté'); }
    catch (error) { toast.error(extractApiError(error)); }
  };
  const search = async () => {
    try { const result = await reimbursementApi.get(reference); setLast(result); toast.success('Prise en charge trouvé'); }
    catch (error) { toast.error(extractApiError(error)); }
  };

  return (
    <div>
      <PageHeader title="Prises en charge" description="Calcul automatique : 100% généraliste, 80% spécialiste. Réservé aux agents." />
      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        <Card><CardHeader title="Effectuer une prise en charge" description="Une feuille de soins ne peut être prise en charge qu'une seule fois." />
          <form className="space-y-4" onSubmit={form.handleSubmit(() => modal.openModal())}>
            <Input label="Numéro feuille de soins" required error={form.formState.errors.sheetNumber?.message} {...form.register('sheetNumber')} />
            <Select label="Mode paiement" required options={[{ label: 'Espèces', value: 'CASH' }, { label: 'Virement bancaire', value: 'BANK_TRANSFER' }]} error={form.formState.errors.paymentType?.message} {...form.register('paymentType')} />
            {paymentType === 'BANK_TRANSFER' && <Input label="IBAN" required error={form.formState.errors.bankIban?.message} {...form.register('bankIban')} />}
            <Button icon={<CreditCard className="h-4 w-4" />}>Calculer et valider</Button>
          </form>
        </Card>
        <Card><CardHeader title="Consulter une référence" />
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end"><Input label="Référence prise en charge" value={reference} onChange={(e) => setReference(e.target.value)} /><Button icon={<Search className="h-4 w-4" />} onClick={search}>Rechercher</Button></div>
          {last && <div className="mt-5 rounded-xl bg-slate-50 p-4 text-sm"><p><strong>Référence :</strong> {last.reimbursementNumber}</p><p><strong>Feuille :</strong> {last.sheetNumber}</p><p><strong>Base :</strong> {last.baseAmount}</p><p><strong>Taux :</strong> {Number(last.rate) * 100}%</p><p><strong>Montant :</strong> {last.reimbursedAmount}</p><div className="mt-2"><Badge tone="success">{last.status}</Badge></div></div>}
        </Card>
      </div>
      <ConfirmModal open={modal.open} title="Confirmer le prise en charge" message="Cette action exécutera le paiement et empêchera un deuxième prise en charge de la même feuille." onClose={modal.closeModal} onConfirm={() => { modal.closeModal(); form.handleSubmit(submit)(); }} />
    </div>
  );
}
