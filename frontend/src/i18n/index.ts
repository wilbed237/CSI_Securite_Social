import i18n from 'i18next';
import { initReactI18next, useTranslation as useReactTranslation } from 'react-i18next';
import fr from '../locales/fr/translation.json';
import en from '../locales/en/translation.json';

export type AppLanguage = 'fr' | 'en';
const LANGUAGE_KEY = 'care-health-language';
const storedLanguage = window.localStorage.getItem(LANGUAGE_KEY) === 'en' ? 'en' : 'fr';

void i18n.use(initReactI18next).init({
  resources: { fr: { translation: fr }, en: { translation: en } },
  lng: storedLanguage,
  fallbackLng: 'fr',
  interpolation: { escapeValue: false },
});

document.documentElement.lang = storedLanguage;

export async function changeLanguage(language: AppLanguage) {
  window.localStorage.setItem(LANGUAGE_KEY, language);
  document.documentElement.lang = language;
  await i18n.changeLanguage(language);
}

export function useTranslation() {
  const result = useReactTranslation();
  return { language: result.i18n.resolvedLanguage === 'en' ? 'en' as const : 'fr' as const, t: (key: string, fallback?: string) => result.t(key, { defaultValue: fallback ?? key }) };
}

export default i18n;
