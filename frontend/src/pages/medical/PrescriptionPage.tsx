import { zodResolver } from '@hookform/resolvers/zod';
import { Pill, Stethoscope } from 'lucide-react';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { z } from 'zod';
import { medicalApi } from '../../api/medicalApi';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { RoleGate } from '../../components/RoleGate';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useTranslation } from '../../i18n';
import type { PrescriptionResponse, ReferralResponse } from '../../types/api';
import { Select } from '../../components/ui/Select';
import { CopyIdButton } from '../../components/ui/CopyIdButton';

const medicationSchema = z.object({
  consultationId: z.string().uuid('ID consultation invalide'),
  medicationName: z.string().min(1, 'Médicament requis'),
  posology: z.string().min(1, 'Posologie requise'),
});
const referralSchema = z.object({
  consultationId: z.string().uuid('ID consultation invalide'),
  specialistMatricule: z.string().min(1, 'Spécialiste requis'),
  reason: z.string().min(3, 'Motif requis'),
  priority: z.enum(['ROUTINE', 'URGENT', 'EMERGENCY']),
});
type MedicationValues = z.infer<typeof medicationSchema>;
type ReferralValues = z.infer<typeof referralSchema>;

export function PrescriptionPage() {
  const { t } = useTranslation();
  usePageTitle(t('prescription.title'));
  const [lastPrescription, setLastPrescription] = useState<PrescriptionResponse | null>(null);
  const [lastReferral, setLastReferral] = useState<ReferralResponse | null>(null);
  const specialists = useQuery({
    queryKey: ['active-specialists'],
    queryFn: () => profileApi.listDoctors({ type: 'SPECIALIST', active: true, size: 100 }),
  });
  const medicationForm = useForm<MedicationValues>({ resolver: zodResolver(medicationSchema) });
  const referralForm = useForm<ReferralValues>({
    resolver: zodResolver(referralSchema),
    defaultValues: { priority: 'ROUTINE' },
  });

  const submitMedication = async (values: MedicationValues) => {
    try {
      const result = await medicalApi.prescribeMedications({
        consultationId: values.consultationId,
        medications: [{ name: values.medicationName, posology: values.posology }],
      });
      setLastPrescription(result);
      toast.success(t('prescription.created'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const submitReferral = async (values: ReferralValues) => {
    const specialist = specialists.data?.content.find((d) => d.matricule === values.specialistMatricule);
    if (!specialist?.specialty) {
      toast.error(t('errors.unexpected'));
      return;
    }
    try {
      const result = await medicalApi.createReferral({
        consultationId: values.consultationId,
        specialty: specialist.specialty,
        reason: values.reason,
        priority: values.priority,
        specialistMatricules: [specialist.matricule],
      });
      setLastReferral(result);
      toast.success(t('prescription.referralCreated'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  return (
    <div>
      <PageHeader title={t('prescription.title')} description={t('prescription.description')} />
      <div className="grid gap-6 xl:grid-cols-2">
        <Card>
          <CardHeader title={t('prescription.medications.title')} description={t('prescription.medications.description')} />
          <form className="space-y-4" onSubmit={medicationForm.handleSubmit(submitMedication)}>
            <Input
              label={t('prescription.consultationId')}
              required
              error={medicationForm.formState.errors.consultationId?.message}
              {...medicationForm.register('consultationId')}
            />
            <Input
              label={t('prescription.medicationName')}
              required
              error={medicationForm.formState.errors.medicationName?.message}
              {...medicationForm.register('medicationName')}
            />
            <Input
              label={t('prescription.posology')}
              required
              error={medicationForm.formState.errors.posology?.message}
              {...medicationForm.register('posology')}
            />
            <Button isLoading={medicationForm.formState.isSubmitting} icon={<Pill className="h-4 w-4" />}>
              {t('prescription.medications.button')}
            </Button>
          </form>
        </Card>

        <RoleGate
          roles={['GENERALIST']}
          fallback={
            <Card>
              <CardHeader
                title={t('prescription.specialist.title')}
                description={t('prescription.specialist.description')}
              />
              <p className="text-sm text-slate-600 dark:text-slate-300">
                {t('prescription.specialist.fallback')}
              </p>
            </Card>
          }
        >
          <Card>
            <CardHeader
              title={t('prescription.specialist.title')}
              description={t('prescription.specialist.description')}
            />
            <form className="space-y-4" onSubmit={referralForm.handleSubmit(submitReferral)}>
              <Input
                label={t('prescription.consultationId')}
                required
                error={referralForm.formState.errors.consultationId?.message}
                {...referralForm.register('consultationId')}
              />
              <Select
                label={t('prescription.specialistSelect')}
                required
                error={referralForm.formState.errors.specialistMatricule?.message}
                options={[
                  {
                    label: specialists.isLoading
                      ? t('prescription.specialist.loading')
                      : t('prescription.specialist.select'),
                    value: '',
                  },
                  ...(specialists.data?.content ?? []).map((doctor) => ({
                    label: `${doctor.firstName} ${doctor.lastName} — ${doctor.specialty ?? t('prescription.specialist.noSpecialty')}`,
                    value: doctor.matricule,
                  })),
                ]}
                {...referralForm.register('specialistMatricule')}
              />
              <Select
                label={t('prescription.priority')}
                required
                options={[
                  { label: t('prescription.priority.ROUTINE'), value: 'ROUTINE' },
                  { label: t('prescription.priority.URGENT'), value: 'URGENT' },
                  { label: t('prescription.priority.EMERGENCY'), value: 'EMERGENCY' },
                ]}
                {...referralForm.register('priority')}
              />
              <Textarea
                label={t('prescription.reason')}
                required
                error={referralForm.formState.errors.reason?.message}
                {...referralForm.register('reason')}
              />
              <Button isLoading={referralForm.formState.isSubmitting} icon={<Stethoscope className="h-4 w-4" />}>
                {t('prescription.specialist.button')}
              </Button>
            </form>
          </Card>
        </RoleGate>
      </div>

      {lastPrescription && (
        <Card className="mt-6">
          <CardHeader title={t('prescription.lastPrescription')} />
          <p className="text-sm"><strong>ID :</strong> <CopyIdButton id={lastPrescription.id} /></p>
          <p className="text-sm"><strong>{t('common.reference')} :</strong> <CopyIdButton id={lastPrescription.prescriptionNumber} /></p>
          <div className="mt-2">
            <Badge tone="primary">{t(`type.${lastPrescription.type}`, lastPrescription.type)}</Badge>
          </div>
        </Card>
      )}

      {lastReferral && (
        <Card className="mt-6">
          <CardHeader title={t('prescription.lastReferral')} />
          <p className="text-sm"><strong>{t('common.reference')} :</strong> <CopyIdButton id={lastReferral.referralNumber} /></p>
          <p className="text-sm"><strong>{t('doctor.specialty')} :</strong> {lastReferral.specialty}</p>
          <div className="mt-2">
            <Badge tone="warning">{t(`status.${lastReferral.status}`, lastReferral.status)}</Badge>
          </div>
        </Card>
      )}
    </div>
  );
}
