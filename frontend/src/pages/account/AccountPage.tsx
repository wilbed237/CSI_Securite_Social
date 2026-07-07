import { useMutation } from '@tanstack/react-query';
import { KeyRound, UserRound } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { authApi } from '../../api/authApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useAuthStore } from '../../store/authStore';

export function AccountPage() {
  const user = useAuthStore((state) => state.user);
  usePageTitle('Mon compte');

  const [username, setUsername] = useState(user?.username ?? '');
  const [email, setEmail] = useState(user?.email ?? '');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);

  const accountMutation = useMutation({
    mutationFn: () => authApi.updateAccount({ username, email, phoneNumber: phoneNumber || undefined }),
    onSuccess: () => {
      toast.success('Informations de compte mises à jour');
      setError(null);
    },
    onError: (err) => {
      const message = extractApiError(err);
      setError(message);
      toast.error(message);
    },
  });

  const passwordMutation = useMutation({
    mutationFn: () => authApi.changePassword({ currentPassword, newPassword }),
    onSuccess: () => {
      toast.success('Mot de passe mis à jour');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setError(null);
    },
    onError: (err) => {
      const message = extractApiError(err);
      setError(message);
      toast.error(message);
    },
  });

  const submitAccount = () => accountMutation.mutate();

  const submitPassword = () => {
    if (newPassword.length < 8) {
      setError('Le nouveau mot de passe doit contenir au moins 8 caractères');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('La confirmation ne correspond pas');
      return;
    }
    passwordMutation.mutate();
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Mon compte" description="Gérez vos informations personnelles et votre mot de passe." />

      {error && <div className="rounded-lg border border-danger-200 bg-danger-50 p-3 text-sm font-medium text-danger-700">{error}</div>}

      <div className="grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
        <Card>
          <div className="flex items-center gap-2">
            <UserRound className="h-5 w-5 text-primary-700" />
            <CardHeader title="Informations du compte" />
          </div>
          <div className="space-y-3">
            <Input label="Nom d'utilisateur" value={username} onChange={(e) => setUsername(e.target.value)} required />
            <Input label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            <Input label="Téléphone" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} />
            <Button onClick={submitAccount} isLoading={accountMutation.isPending}>Enregistrer les informations</Button>
          </div>
        </Card>

        <Card>
          <div className="flex items-center gap-2">
            <KeyRound className="h-5 w-5 text-primary-700" />
            <CardHeader title="Sécurité" />
          </div>
          <div className="space-y-3">
            <Input label="Mot de passe actuel" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
            <Input label="Nouveau mot de passe" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
            <Input label="Confirmer le nouveau mot de passe" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
            <Button onClick={submitPassword} isLoading={passwordMutation.isPending}>Changer le mot de passe</Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
