import { Languages } from 'lucide-react';
import toast from 'react-hot-toast';
import { changeLanguage, useTranslation, type AppLanguage } from '../../i18n';
import { Select } from '../ui/Select';

export function LanguageSetting() {
  const { language, t } = useTranslation();
  const change = (next: AppLanguage) => { void changeLanguage(next); toast.success(next === 'fr' ? 'Langue mise à jour' : 'Language updated'); };
  return <div className="rounded-lg border border-slate-200 p-4 transition hover:border-primary-300 focus-within:ring-2 focus-within:ring-primary-500"><div className="mb-3 flex items-center gap-3"><Languages className="h-5 w-5 text-primary-700" /><div><h3 className="font-bold text-slate-950">{t('settings.language')}</h3><p className="text-sm text-slate-500">{language === 'fr' ? 'Français' : 'English'}</p></div></div><Select label={t('settings.language')} value={language} onChange={(event) => change(event.target.value as AppLanguage)} options={[{ label: t('language.fr'), value: 'fr' }, { label: t('language.en'), value: 'en' }]} /></div>;
}
