import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../../api/authApi';
import { extractApiError } from '../../api/httpClient';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { usePageTitle } from '../../hooks/usePageTitle';
import type { RoleName } from '../../types/api';

const schema = z.object({
  username: z.string().min(3, 'Nom utilisateur trop court'),
  email: z.string().email('Email invalide'),
  phoneNumber: z.string().optional(),
  password: z.string().min(8, '8 caractères minimum'),
  role: z.enum(['AGENT', 'GENERALIST', 'SPECIALIST']),
});
type FormValues = z.infer<typeof schema>;

const roleOptions = [
  { label: 'Agent sécurité sociale', value: 'AGENT' },
  { label: 'Médecin généraliste', value: 'GENERALIST' },
  { label: 'Médecin spécialiste', value: 'SPECIALIST' },
];

export function RegisterPage() {
  usePageTitle('Inscription');
  const navigate = useNavigate();
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { role: 'AGENT' } });

  const rolesFromSelection = (role: FormValues['role']): RoleName[] => role === 'AGENT' ? ['AGENT'] : ['DOCTOR', role];

  const onSubmit = async (values: FormValues) => {
    try {
      await authApi.register({ username: values.username, email: values.email, phoneNumber: values.phoneNumber, password: values.password, roles: rolesFromSelection(values.role) });
      toast.success('Utilisateur créé. Vous pouvez vous connecter.');
      navigate('/login');
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  return (
    <Card className="w-full max-w-lg">
      <h1 className="text-2xl font-black text-slate-950">Créer un utilisateur</h1>
      <p className="mt-2 text-sm text-slate-500">Endpoint backend prévu pour ajouter des comptes applicatifs.</p>
      <form className="mt-6 grid gap-4 sm:grid-cols-2" onSubmit={handleSubmit(onSubmit)}>
        <Input label="Nom utilisateur" required error={errors.username?.message} {...register('username')} />
        <Input label="Email" type="email" required error={errors.email?.message} {...register('email')} />
        <Input label="Téléphone" error={errors.phoneNumber?.message} {...register('phoneNumber')} />
        <Select label="Profil" required options={roleOptions} error={errors.role?.message} {...register('role')} />
        <Input className="sm:col-span-2" label="Mot de passe" type="password" required error={errors.password?.message} {...register('password')} />
        <Button className="sm:col-span-2" isLoading={isSubmitting}>Créer le compte</Button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-500"><Link className="font-semibold text-primary-700" to="/login">Retour connexion</Link></p>
    </Card>
  );
}
