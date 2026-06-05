import { Moon, Palette, Sun } from 'lucide-react';
import { THEME_OPTIONS, useThemeStore } from '../store/themeStore';
import { cn } from '../utils/cn';

export function ThemeToggle({ className }: { className?: string }) {
  const { theme, cycleTheme } = useThemeStore();
  const activeTheme = THEME_OPTIONS.find((option) => option.value === theme) ?? THEME_OPTIONS[0];
  const isLight = theme === 'light';

  return (
    <button
      type="button"
      onClick={cycleTheme}
      className={cn(
        'theme-toggle button-press inline-flex items-center gap-2 rounded-full border border-white/20 bg-white/10 px-3 py-2 text-sm font-semibold text-white shadow-sm backdrop-blur transition hover:bg-white/20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-secondary-100 dark:border-slate-700 dark:bg-slate-900/80 dark:text-slate-100 dark:hover:bg-slate-800',
        className,
      )}
      aria-label={`Changer de thème. Thème actuel : ${activeTheme.label}`}
      title={`Thème actuel : ${activeTheme.label}`}
    >
      {isLight ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
      <Palette className="h-4 w-4 opacity-75" />
      <span className="hidden sm:inline">{activeTheme.shortLabel}</span>
    </button>
  );
}
