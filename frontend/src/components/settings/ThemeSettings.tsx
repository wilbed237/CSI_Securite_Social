import { useEffect, useState } from 'react';
import { Select } from '../ui/Select';
import { THEME_OPTIONS, useThemeStore } from '../../store/themeStore';
import type { AnimationMode, DensityMode, UiPreferences } from '../../types/settings';

const UI_PREFS_KEY = 'care-health-ui-preferences';
const defaultPrefs: UiPreferences = { density: 'comfortable', animations: 'normal' };

function readPrefs(): UiPreferences {
  try {
    const stored = window.localStorage.getItem(UI_PREFS_KEY);
    return stored ? { ...defaultPrefs, ...JSON.parse(stored) } : defaultPrefs;
  } catch {
    return defaultPrefs;
  }
}

export function ThemeSettings() {
  const { theme, setTheme } = useThemeStore();
  const [prefs, setPrefs] = useState<UiPreferences>(readPrefs);

  useEffect(() => {
    window.localStorage.setItem(UI_PREFS_KEY, JSON.stringify(prefs));
    document.documentElement.dataset.density = prefs.density;
    document.documentElement.dataset.animations = prefs.animations;
  }, [prefs]);

  return (
    <div className="grid gap-4 md:grid-cols-3">
      <Select label="Thème actif" value={theme} onChange={(event) => setTheme(event.target.value as typeof theme)} options={THEME_OPTIONS.map((option) => ({ label: option.label, value: option.value }))} />
      <Select label="Densité" value={prefs.density} onChange={(event) => setPrefs((current) => ({ ...current, density: event.target.value as DensityMode }))} options={[{ label: 'Confortable', value: 'comfortable' }, { label: 'Compacte', value: 'compact' }]} />
      <Select label="Animations" value={prefs.animations} onChange={(event) => setPrefs((current) => ({ ...current, animations: event.target.value as AnimationMode }))} options={[{ label: 'Normales', value: 'normal' }, { label: 'Réduites', value: 'reduced' }]} />
    </div>
  );
}
