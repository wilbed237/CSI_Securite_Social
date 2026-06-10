import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { z } from 'zod';
import { medicalApi } from '../../api/medicalApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { CopyIdButton } from '../../components/ui/CopyIdButton';
import { usePageTitle } from '../../hooks/usePageTitle';
import type { ConsultationResponse } from '../../types/api';

const schema = z.object({
  insuranceNumber: z.string().trim().min(1, "Le numéro d'assuré est obligatoire"),
  startedAt: z.string().min(1, 'La date de début est obligatoire'),
  endedAt: z.string().min(1, 'La date de fin est obligatoire'),
  cost: z.number().positive('Le coût doit être supérieur à zéro'),
}).refine((values) => !values.startedAt || !values.endedAt || values.endedAt > values.startedAt, {
  message: 'La fin doit être postérieure au début',
  path: ['endedAt'],
});
type FormValues = z.infer<typeof schema>;

export function ConsultationCreatePage() {
  usePageTitle('Consultation');
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { insuranceNumber: 'ASS-0001', cost: 10000 } });
  const [created, setCreated] = useState<ConsultationResponse | null>(null);
  async function onSubmit(values: FormValues) {
    try { const consultation = await medicalApi.createConsultation(values); setCreated(consultation); toast.success('Consultation créée'); }
    catch (error) { toast.error(extractApiError(error)); }
  }
  return (
    <div>
      <PageHeader title="Créer une consultation" description="Le patient est vérifié comme assuré actif. Le médecin est identifié à partir du compte connecté." />
      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        <Card><form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Numéro patient couvert" required error={errors.insuranceNumber?.message} {...register('insuranceNumber')} />
          <Input label="Coût consultation" type="number" required error={errors.cost?.message} {...register('cost', { valueAsNumber: true })} />
          <Input label="Début" type="datetime-local" required error={errors.startedAt?.message} {...register('startedAt')} />
          <Input label="Fin" type="datetime-local" required error={errors.endedAt?.message} {...register('endedAt')} />
          <Button className="md:col-span-2" isLoading={isSubmitting}>Créer la consultation</Button>
        </form></Card>
        <Card><CardHeader title="Consultation créée" description="Copiez l'identifiant pour les ordonnances et feuilles de soins." />{created ? <div className="space-y-3 text-sm"><p><strong>ID :</strong> <CopyIdButton id={created.id} /></p><p><strong>Patient couvert :</strong> {created.insuranceNumber}</p><Badge tone="success">{created.doctorType}</Badge></div> : <p className="text-sm text-slate-500">Aucune consultation créée dans cette session.</p>}</Card>
      </div>
    </div>
  );
}
