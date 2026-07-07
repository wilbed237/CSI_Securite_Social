import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Activity,
  Globe,
  Info,
  Languages,
  Palette,
  Pencil,
  Save,
  Shield,
  Stethoscope,
  X,
} from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { authApi } from '../../api/authApi';
import { dashboardApi } from '../../api/dashboardApi';
import { settingsApi } from '../../api/settingsApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { CountrySetting } from '../../components/settings/CountrySetting';
import { CurrencyActivitySetting } from '../../components/settings/CurrencyActivitySetting';
import { InsuranceSettings } from '../../components/settings/InsuranceSettings';
import { LanguageSetting } from '../../components/settings/LanguageSetting';
import { MedicalSettings } from '../../components/settings/MedicalSettings';
import { ThemeSettings } from '../../components/settings/ThemeSettings';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { ErrorState } from '../../components/ui/ErrorState';
import { Input } from '../../components/ui/Input';
import { Loader } from '../../components/ui/Loader';
import { Badge } from '../../components/ui/Badge';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useTranslation } from '../../i18n';
import { useAuthStore } from '../../store/authStore';
import { env } from '../../config/env';
import type { SettingResponse, SettingsCategory } from '../../types/settings';

function settingsFor(
  settings: { category: string; settings: SettingResponse[] }[] | undefined,
  category: SettingsCategory,
) {
  return settings?.find((item) => item.category === category)?.settings ?? [];
}

function SettingItem({ setting }: { setting: SettingResponse }) {
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [editing, setEditing] = useState(false);
  const [value, setValue] = useState('');
  const mutation = useMutation({
    mutationFn: () =>
      settingsApi.upsert(setting.category as SettingsCategory, {
        key: setting.key,
        value,
        description: setting.description,
        active: setting.active,
      }),
    onSuccess: () => {
      toast.success(t('settings.saved'));
      setEditing(false);
      void queryClient.invalidateQueries({ queryKey: ['settings'] });
    },
    onError: (error) => toast.error(extractApiError(error)),
  });

  return (
    <div className="rounded-lg border border-slate-200 p-4 transition hover:border-primary-300">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="font-semibold text-slate-900">
            {t(`technical.${setting.key}`, setting.description ?? setting.key.replaceAll('_', ' '))}
          </p>
          {!editing && (
            <p className="mt-1 text-sm text-slate-600">
              {t(`technical.${setting.value}`, setting.value)}
            </p>
          )}
          {setting.description && (
            <p className="mt-0.5 text-xs text-slate-400">{setting.description}</p>
          )}
        </div>
        {!editing && (
          <Button
            variant="ghost"
            aria-label={`${t('settings.modify')} ${setting.key}`}
            icon={<Pencil className="h-4 w-4" />}
            onClick={() => {
              setEditing(true);
              setValue(setting.value);
            }}
          >
            {t('settings.modify')}
          </Button>
        )}
      </div>
      {editing && (
        <div className="mt-3 flex items-end gap-2">
          <Input label="Valeur" value={value} onChange={(e) => setValue(e.target.value)} />
          <Button
            aria-label={t('common.save')}
            icon={<Save className="h-4 w-4" />}
            isLoading={mutation.isPending}
            onClick={() => mutation.mutate()}
          />
          <Button
            aria-label={t('common.cancel')}
            variant="secondary"
            icon={<X className="h-4 w-4" />}
            onClick={() => setEditing(false)}
          />
        </div>
      )}
    </div>
  );
}

function SettingsSection({
  icon: Icon,
  title,
  description,
  children,
}: {
  icon: React.ElementType;
  title: string;
  description?: string;
  children: React.ReactNode;
}) {
  return (
    <section>
      <div className="mb-4 flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary-50">
          <Icon className="h-5 w-5 text-primary-700" />
        </div>
        <div>
          <h2 className="text-base font-bold text-slate-900">{title}</h2>
          {description && <p className="text-sm text-slate-500">{description}</p>}
        </div>
      </div>
      {children}
    </section>
  );
}

function ChangePasswordCard() {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const mutation = useMutation({
    mutationFn: () => authApi.changePassword({ currentPassword, newPassword }),
    onSuccess: () => {
      toast.success('Mot de passe mis à jour');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setError(null);
    },
    onError: (err) => {
      setError(extractApiError(err));
      toast.error(extractApiError(err));
    },
  });

  const submit = () => {
    if (newPassword.length < 8) {
      setError('Le nouveau mot de passe doit contenir au moins 8 caractères');
      return;
    }
    if (newPassword !== confirmPassword) {
      setError('La confirmation ne correspond pas');
      return;
    }
    setError(null);
    mutation.mutate();
  };

  return (
    <Card>
      <CardHeader title="Changer le mot de passe" />
      <div className="space-y-3">
        <Input label="Mot de passe actuel" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
        <Input label="Nouveau mot de passe" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
        <Input label="Confirmer le nouveau mot de passe" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
        {error && <p className="text-sm font-medium text-danger-500">{error}</p>}
        <Button onClick={submit} isLoading={mutation.isPending}>Enregistrer</Button>
      </div>
    </Card>
  );
}

function ReimbursementRatesCard() {
  const { t } = useTranslation();
  return (
    <Card>
      <CardHeader title={t('settings.sections.reimbursement')} />
      <div className="grid gap-3 sm:grid-cols-2">
        <div className="rounded-lg border border-slate-200 p-4 bg-slate-50">
          <p className="text-sm text-slate-500">{t('settings.rate.generalist')}</p>
          <p className="mt-1 text-2xl font-black text-primary-700">{t('settings.rate.generalistValue')}</p>
          <p className="mt-1 text-xs text-slate-400">{t('technical.GENERALIST')}</p>
        </div>
        <div className="rounded-lg border border-slate-200 p-4 bg-slate-50">
          <p className="text-sm text-slate-500">{t('settings.rate.specialist')}</p>
          <p className="mt-1 text-2xl font-black text-primary-700">{t('settings.rate.specialistValue')}</p>
          <p className="mt-1 text-xs text-slate-400">{t('technical.SPECIALIST')}</p>
        </div>
      </div>
      <div className="mt-3 grid gap-2 sm:grid-cols-2 text-sm">
        <div className="flex items-center gap-2">
          <Badge tone="success">{t('technical.CASH')}</Badge>
          <Badge tone="primary">{t('technical.BANK_TRANSFER')}</Badge>
        </div>
      </div>
    </Card>
  );
}

export function SettingsPage() {
  const { t } = useTranslation();
  const user = useAuthStore((state) => state.user);
  usePageTitle(t('settings.title'));
  const query = useQuery({ queryKey: ['settings'], queryFn: settingsApi.listAll });
  const finance = useQuery({
    queryKey: ['settings', 'finance'],
    queryFn: () => dashboardApi.getAgentReimbursementsStats({ period: 'MONTH' }),
  });

  if (query.isLoading) return <Loader />;
  if (query.isError) return <ErrorState message={extractApiError(query.error)} />;

  const general = settingsFor(query.data, 'GENERAL');
  const country = general.find((item) => item.key === 'COUNTRY')?.value ?? 'CM';
  const filteredGeneral = general.filter(
    (item) => !['APPLICATION_NAME', 'COUNTRY', 'CURRENCY', 'LANGUAGE'].includes(item.key),
  );

  return (
    <div className="space-y-8">
      <PageHeader title={t('settings.title')} description={t('settings.description')} />

      {/* Application et compte */}
      <SettingsSection icon={Info} title={t('settings.sections.appAccount')}>
        <div className="rounded-lg border border-slate-200 p-5 space-y-3">
          <div className="grid gap-3 sm:grid-cols-2 text-sm">
            <div>
              <p className="text-xs text-slate-500">{t('settings.appName')}</p>
              <p className="font-semibold">{env.appName}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">{t('settings.appVersion')}</p>
              <p className="font-semibold">v{env.appVersion}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">{t('settings.environment')}</p>
              <p className="font-semibold">{env.environment}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">{t('settings.currentUser')}</p>
              <p className="font-semibold">{user?.username}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">Email</p>
              <p className="font-semibold">{user?.email ?? '—'}</p>
            </div>
            <div>
              <p className="text-xs text-slate-500">{t('settings.roles')}</p>
              <div className="mt-1 flex flex-wrap gap-1">
                {user?.roles.map((role) => (
                  <Badge key={role} tone="primary">{t(`technical.${role}`, role)}</Badge>
                ))}
              </div>
            </div>
          </div>
        </div>
      </SettingsSection>

      {/* Langue et région */}
      <SettingsSection icon={Languages} title={t('settings.sections.langRegion')}>
        <div className="grid gap-4 xl:grid-cols-2">
          <LanguageSetting />
          <CountrySetting defaultCountry={country} />
        </div>
      </SettingsSection>

      {/* Devise et activité */}
      <SettingsSection icon={Globe} title={t('settings.currencyActivity')}>
        <CurrencyActivitySetting stats={finance.data} />
      </SettingsSection>

      {/* Préférences d'interface */}
      <SettingsSection icon={Palette} title={t('settings.sections.interface')}>
        <Card>
          <ThemeSettings />
        </Card>
      </SettingsSection>

      {/* Paramètres médicaux */}
      <SettingsSection icon={Stethoscope} title={t('settings.sections.medical')}>
        <Card>
          <MedicalSettings settings={settingsFor(query.data, 'MEDICAL')} />
        </Card>
      </SettingsSection>

      {/* Paramètres de remboursement */}
      <SettingsSection icon={Activity} title={t('settings.sections.reimbursement')}>
        <div className="grid gap-4 xl:grid-cols-2">
          <ReimbursementRatesCard />
          <Card>
            <CardHeader title={t('settings.insurance')} />
            <InsuranceSettings settings={settingsFor(query.data, 'INSURANCE')} />
          </Card>
        </div>
      </SettingsSection>

      {/* Sécurité */}
      <SettingsSection icon={Shield} title={t('settings.security')}>
        <div className="mb-4">
          <ChangePasswordCard />
        </div>
        {filteredGeneral.length > 0 ? (
          <div className="grid gap-3 md:grid-cols-2">
            {filteredGeneral.map((s) => <SettingItem key={s.id} setting={s} />)}
          </div>
        ) : (
          <p className="text-sm text-slate-500">{t('states.emptyDescription')}</p>
        )}
        <div className="mt-3 grid gap-3 md:grid-cols-2">
          {settingsFor(query.data, 'SECURITY').map((s) => <SettingItem key={s.id} setting={s} />)}
        </div>
      </SettingsSection>
    </div>
  );
}
