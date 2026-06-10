import { Info, UserRound } from 'lucide-react';
import { env } from '../../config/env';
import { useAuthStore } from '../../store/authStore';
import { useTranslation } from '../../i18n';
import { Badge } from '../ui/Badge';

export function ApplicationAccountSetting() {
  const user = useAuthStore((state) => state.user);
  const { t } = useTranslation();

  return (
    <div className="rounded-lg border border-slate-200 p-4 transition hover:border-primary-300">
      <div className="flex items-center gap-3">
        <Info className="h-5 w-5 text-primary-700" />
        <div>
          <h3 className="font-bold text-slate-950">{t('settings.sections.appAccount')}</h3>
          <p className="text-sm text-slate-500">
            {env.appName} · Clinical Suite · v{env.appVersion} · {env.environment}
          </p>
        </div>
      </div>
      <div className="mt-4 grid gap-2 text-sm sm:grid-cols-2">
        <p><strong>{t('settings.currentUser')} :</strong> {user?.username}</p>
        <p><strong>Email :</strong> {user?.email}</p>
        <p><strong>{t('insured.phone')} :</strong> {user?.phoneNumber ?? '—'}</p>
        <p><strong>{t('settings.appName')} :</strong> {env.appName}</p>
        <p><strong>{t('settings.appVersion')} :</strong> {env.appVersion}</p>
        <p><strong>{t('settings.environment')} :</strong> {env.environment}</p>
      </div>
      <div className="mt-3 flex flex-wrap items-center gap-2">
        <UserRound className="h-4 w-4 text-slate-500" />
        <span className="text-xs text-slate-500">{t('settings.roles')} :</span>
        {user?.roles.map((role) => (
          <Badge key={role} tone="primary">
            {t(`technical.${role}`, role)}
          </Badge>
        ))}
      </div>
    </div>
  );
}
