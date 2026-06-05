import { zodResolver } from '@hookform/resolvers/zod';
import { useForm, useWatch } from 'react-hook-form';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { usePageTitle } from '../../hooks/usePageTitle';

const schema = z.object({ matricule: z.string().min(2), firstName: z.string().min(1), lastName: z.string().min(1), type: z.enum(['GENERALIST', 'SPECIALIST']), specialty: z.string().optional(), phoneNumber: z.string().optional(), email: z.string().email().optional().or(z.literal('')) }).superRefine((value, ctx) => {
  if (value.type === 'SPECIALIST' && !value.specialty) ctx.addIssue({ code: 'custom', path: ['specialty'], message: 'Spécialité obligatoire pour un spécialiste' });
  if (value.type === 'GENERALIST' && value.specialty) ctx.addIssue({ code: 'custom', path: ['specialty'], message: 'Un généraliste ne doit pas avoir de spécialité' });
});
type FormValues = z.infer<typeof schema>;

export function DoctorCreatePage() {
  usePageTitle('Nouveau médecin');
  const navigate = useNavigate();
  const { register, control, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { type: 'GENERALIST', matricule: 'MED-GEN-002' } });
  const type = useWatch({ control, name: 'type' });
  const onSubmit = async (values: FormValues) => {
    try { await profileApi.createDoctor({ ...values, specialty: values.specialty || undefined, email: values.email || undefined }); toast.success('Médecin enregistré'); navigate('/app/doctors'); }
    catch (error) { toast.error(extractApiError(error)); }
  };
  return (
    <div>
      <PageHeader title="Enregistrer un médecin" description="Respecte la règle d'exclusivité généraliste/spécialiste du référentiel clinique." />
      <Card>
        <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Matricule" required error={errors.matricule?.message} {...register('matricule')} />
          <Select label="Type" required options={[{ label: 'Généraliste', value: 'GENERALIST' }, { label: 'Spécialiste', value: 'SPECIALIST' }]} error={errors.type?.message} {...register('type')} />
          <Input label="Prénom" required error={errors.firstName?.message} {...register('firstName')} />
          <Input label="Nom" required error={errors.lastName?.message} {...register('lastName')} />
          <Input label="Spécialité" disabled={type === 'GENERALIST'} helper="Obligatoire uniquement pour un spécialiste" error={errors.specialty?.message} {...register('specialty')} />
          <Input label="Téléphone" error={errors.phoneNumber?.message} {...register('phoneNumber')} />
          <Input label="Email" type="email" error={errors.email?.message} {...register('email')} />
          <Button className="md:col-span-2" isLoading={isSubmitting}>Enregistrer le médecin</Button>
        </form>
      </Card>
    </div>
  );
}
