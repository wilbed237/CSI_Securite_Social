import { zodResolver } from '@hookform/resolvers/zod';
import { Pill, Stethoscope } from 'lucide-react';
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
import type { PrescriptionResponse } from '../../types/api';

const medicationSchema = z.object({ consultationId: z.string().uuid('ID consultation invalide'), medicationName: z.string().min(1), posology: z.string().min(1) });
const referralSchema = z.object({ consultationId: z.string().uuid('ID consultation invalide'), requiredSpecialty: z.string().min(1), factors: z.string().optional() });
type MedicationValues = z.infer<typeof medicationSchema>;
type ReferralValues = z.infer<typeof referralSchema>;

export function PrescriptionPage() {
  usePageTitle('Prescriptions');
  const [lastPrescription, setLastPrescription] = useState<PrescriptionResponse | null>(null);
  const medicationForm = useForm<MedicationValues>({ resolver: zodResolver(medicationSchema) });
  const referralForm = useForm<ReferralValues>({ resolver: zodResolver(referralSchema) });

  const submitMedication = async (values: MedicationValues) => {
    try { const result = await medicalApi.prescribeMedications({ consultationId: values.consultationId, medications: [{ name: values.medicationName, posology: values.posology }] }); setLastPrescription(result); toast.success('Prescription médicamenteuse créée'); }
    catch (error) { toast.error(extractApiError(error)); }
  };
  const submitReferral = async (values: ReferralValues) => {
    try { const result = await medicalApi.prescribeSpecialistConsultation(values); setLastPrescription(result); toast.success('Orientation spécialiste créée'); }
    catch (error) { toast.error(extractApiError(error)); }
  };

  return (
    <div>
      <PageHeader title="Prescriptions" description="Créer une prescription de médicaments ou orienter vers un spécialiste après consultation." />
      <div className="grid gap-6 xl:grid-cols-2">
        <Card>
          <CardHeader title="Médicaments" description="Disponible pour tout médecin authentifié." />
          <form className="space-y-4" onSubmit={medicationForm.handleSubmit(submitMedication)}>
            <Input label="ID consultation" required error={medicationForm.formState.errors.consultationId?.message} {...medicationForm.register('consultationId')} />
            <Input label="Médicament" required error={medicationForm.formState.errors.medicationName?.message} {...medicationForm.register('medicationName')} />
            <Input label="Posologie" required error={medicationForm.formState.errors.posology?.message} {...medicationForm.register('posology')} />
            <Button isLoading={medicationForm.formState.isSubmitting} icon={<Pill className="h-4 w-4" />}>Prescrire médicament</Button>
          </form>
        </Card>
        <Card>
          <CardHeader title="Consultation spécialiste" description="Réservé au rôle GENERALIST côté backend." />
          <form className="space-y-4" onSubmit={referralForm.handleSubmit(submitReferral)}>
            <Input label="ID consultation" required error={referralForm.formState.errors.consultationId?.message} {...referralForm.register('consultationId')} />
            <Input label="Spécialité requise" required error={referralForm.formState.errors.requiredSpecialty?.message} {...referralForm.register('requiredSpecialty')} />
            <Textarea label="Facteurs / justification" error={referralForm.formState.errors.factors?.message} {...referralForm.register('factors')} />
            <Button isLoading={referralForm.formState.isSubmitting} icon={<Stethoscope className="h-4 w-4" />}>Orienter vers spécialiste</Button>
          </form>
        </Card>
      </div>
      {lastPrescription && <Card className="mt-6"><CardHeader title="Dernière prescription" /><p className="text-sm"><strong>Numéro :</strong> {lastPrescription.prescriptionNumber}</p><div className="mt-2"><Badge tone="primary">{lastPrescription.type}</Badge></div></Card>}
    </div>
  );
}
