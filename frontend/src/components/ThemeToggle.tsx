import { Moon, Palette, Sun } from 'lucide-react';
import { THEME_OPTIONS, useThemeStore, type ThemeMode } from '../store/themeStore';
import { cn } from '../utils/cn';

export function ThemeToggle({ className }: { className?: string }) {
  const { theme, cycleTheme, setTheme } = useThemeStore();
  const activeTheme = THEME_OPTIONS.find((option) => option.value === theme) ?? THEME_OPTIONS[0];
  const isLight = theme === 'light';

  return (
    <div
      className={cn(
        'theme-toggle inline-flex items-center overflow-hidden rounded-full border border-white/20 bg-white/10 text-sm font-semibold text-white shadow-sm backdrop-blur transition hover:bg-white/20 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-100 dark:hover:bg-slate-800',
        className,
      )}
      title={`Thème actuel : ${activeTheme.label}`}
    >
      <button
        type="button"
        onClick={cycleTheme}
        className="button-press inline-flex items-center gap-2 px-3 py-2 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-secondary-100"
        aria-label={`Changer de thème. Thème actuel : ${activeTheme.label}`}
      >
        {isLight ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        <Palette className="h-4 w-4 opacity-75" />
      </button>
      <label className="sr-only" htmlFor="care-health-theme-select">Choisir un thème</label>
      <select
        id="care-health-theme-select"
        value={theme}
        onChange={(event) => setTheme(event.target.value as ThemeMode)}
        className="max-w-28 border-0 bg-transparent py-2 pl-1 pr-3 text-sm font-semibold text-current outline-none sm:max-w-none"
        aria-label="Choisir un thème Care Health"
      >
        {THEME_OPTIONS.map((option) => (
          <option key={option.value} value={option.value} className="bg-white text-slate-900 dark:bg-slate-900 dark:text-slate-100">
            {option.shortLabel}
          </option>
        ))}
      </select>
    </div>
  );
}
