import { zodResolver } from '@hookform/resolvers/zod';
import { FilePlus, Search } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { z } from 'zod';
import { medicalApi } from '../../api/medicalApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { usePageTitle } from '../../hooks/usePageTitle';
import type { DiseaseSheetResponse } from '../../types/api';

const createSchema = z.object({ consultationId: z.string().uuid('ID consultation invalide'), diagnosis: z.string().min(3, 'Diagnostic requis') });
type CreateValues = z.infer<typeof createSchema>;

export function DiseaseSheetPage() {
  usePageTitle('Feuilles de soins');
  const [sheetNumber, setSheetNumber] = useState('');
  const [sheet, setSheet] = useState<DiseaseSheetResponse | null>(null);
  const form = useForm<CreateValues>({ resolver: zodResolver(createSchema) });

  const create = async (values: CreateValues) => {
    try { const result = await medicalApi.createDiseaseSheet(values); setSheet(result); setSheetNumber(result.sheetNumber); toast.success('Feuille de soins créée'); }
    catch (error) { toast.error(extractApiError(error)); }
  };
  const search = async () => {
    try { const result = await medicalApi.getDiseaseSheet(sheetNumber); setSheet(result); toast.success('Feuille trouvée'); }
    catch (error) { toast.error(extractApiError(error)); }
  };

  return (
    <div>
      <PageHeader title="Feuilles de soins" description="Document central qui synthétise la consultation et sert de base au prise en charge." />
      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        <Card>
          <CardHeader title="Créer une feuille" description="Nécessite une consultation existante." />
          <form className="space-y-4" onSubmit={form.handleSubmit(create)}>
            <Input label="ID consultation" required error={form.formState.errors.consultationId?.message} {...form.register('consultationId')} />
            <Textarea label="Diagnostic" required error={form.formState.errors.diagnosis?.message} {...form.register('diagnosis')} />
            <Button isLoading={form.formState.isSubmitting} icon={<FilePlus className="h-4 w-4" />}>Créer feuille</Button>
          </form>
        </Card>
        <Card>
          <CardHeader title="Rechercher une feuille" />
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end"><Input label="Numéro feuille" value={sheetNumber} onChange={(e) => setSheetNumber(e.target.value)} /><Button icon={<Search className="h-4 w-4" />} onClick={search}>Rechercher</Button></div>
          {sheet && <div className="mt-5 rounded-xl bg-slate-50 p-4 text-sm"><p><strong>Numéro :</strong> {sheet.sheetNumber}</p><p><strong>Patient couvert :</strong> {sheet.insuranceNumber}</p><p><strong>Médecin :</strong> {sheet.doctorMatricule}</p><p><strong>Coût :</strong> {sheet.consultationCost}</p><div className="mt-2"><Badge tone="success">{sheet.status}</Badge></div></div>}
        </Card>
      </div>
    </div>
  );
}
