import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { profileApi } from '../../api/profileApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { usePageTitle } from '../../hooks/usePageTitle';

const schema = z.object({ insuranceNumber: z.string().min(2), firstName: z.string().min(1), lastName: z.string().min(1), birthDate: z.string().min(1), address: z.string().min(3), phoneNumber: z.string().optional(), email: z.string().email().optional().or(z.literal('')) });
type FormValues = z.infer<typeof schema>;

export function InsuredCreatePage() {
  usePageTitle('Nouvel patient couvert');
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { insuranceNumber: 'ASS-0002' } });
  const onSubmit = async (values: FormValues) => {
    try {
      const insured = await profileApi.createInsured({ ...values, email: values.email || undefined });
      toast.success('Patient couvert inscrit');
      navigate(`/app/insured/${insured.insuranceNumber}`);
    } catch (error) { toast.error(extractApiError(error)); }
  };
  return (
    <div>
      <PageHeader title="Inscrire un patient couvert" description="Fonction réservée aux agents de coordination médicale." />
      <Card>
        <form className="grid gap-4 md:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
          <Input label="Identifiant patient" required error={errors.insuranceNumber?.message} {...register('insuranceNumber')} />
          <Input label="Date naissance" type="date" required error={errors.birthDate?.message} {...register('birthDate')} />
          <Input label="Prénom" required error={errors.firstName?.message} {...register('firstName')} />
          <Input label="Nom" required error={errors.lastName?.message} {...register('lastName')} />
          <Input className="md:col-span-2" label="Adresse" required error={errors.address?.message} {...register('address')} />
          <Input label="Téléphone" error={errors.phoneNumber?.message} {...register('phoneNumber')} />
          <Input label="Email" type="email" error={errors.email?.message} {...register('email')} />
          <Button className="md:col-span-2" isLoading={isSubmitting}>Enregistrer l'patient couvert</Button>
        </form>
      </Card>
    </div>
  );
}
