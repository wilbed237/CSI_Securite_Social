import { zodResolver } from '@hookform/resolvers/zod';
import { Lock, Mail } from 'lucide-react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../../api/authApi';
import { extractApiError } from '../../api/httpClient';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useAuthStore } from '../../store/authStore';

const schema = z.object({ identifier: z.string().min(1, 'Email, téléphone ou utilisateur requis'), password: z.string().min(1, 'Mot de passe requis') });
type FormValues = z.infer<typeof schema>;

export function LoginPage() {
  usePageTitle('Connexion');
  const navigate = useNavigate();
  const location = useLocation();
  const token = useAuthStore((state) => state.accessToken);
  const setSession = useAuthStore((state) => state.setSession);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({ resolver: zodResolver(schema), defaultValues: { identifier: 'agent.csi', password: 'Password123!' } });
  if (token) return <Navigate to="/app" replace />;

  const onSubmit = async (values: FormValues) => {
    try {
      const session = await authApi.login(values);
      setSession(session);
      toast.success('Connexion réussie');
      const target = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/app';
      navigate(target, { replace: true });
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  return (
    <Card className="w-full max-w-md">
      <h1 className="text-2xl font-black text-slate-950">Connexion</h1>
      <p className="mt-2 text-sm text-slate-500">Accédez à votre espace agent ou médecin.</p>
      <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)}>
        <Input label="Identifiant" icon={<Mail className="h-4 w-4" />} error={errors.identifier?.message} required {...register('identifier')} />
        <Input label="Mot de passe" type="password" icon={<Lock className="h-4 w-4" />} error={errors.password?.message} required {...register('password')} />
        <Button className="w-full" isLoading={isSubmitting}>Se connecter</Button>
      </form>
      <p className="mt-5 text-center text-sm text-slate-500">Pas encore de compte ? <Link className="font-semibold text-primary-700" to="/register">Créer un utilisateur</Link></p>
    </Card>
  );
}
