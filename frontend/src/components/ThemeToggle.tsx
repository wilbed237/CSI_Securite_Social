import { Check, ChevronDown, Moon, Palette, Sun } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { THEME_OPTIONS, useThemeStore, type ThemeMode } from '../store/themeStore';
import { cn } from '../utils/cn';

const themePreview: Record<ThemeMode, string> = {
  light: 'linear-gradient(135deg, #ffffff, #E0F2FE, #22C55E)',
  'dark-blue': 'linear-gradient(135deg, #020617, #38BDF8, #14B8A6)',
  'dark-orange': 'linear-gradient(135deg, #1C1208, #FB923C, #FACC15)',
  'dark-purple': 'linear-gradient(135deg, #120A1F, #A855F7, #EC4899)',
  'dark-black': 'linear-gradient(135deg, #000000, #111827, #22C55E)',
  'dark-emerald': 'linear-gradient(135deg, #03140c, #22C55E, #86efac)',
  'dark-cyan': 'linear-gradient(135deg, #04131a, #06B6D4, #67E8F9)',
  'dark-rose': 'linear-gradient(135deg, #1f0710, #F43F5E, #60A5FA)',
};

export function ThemeToggle({ className }: { className?: string }) {
  const { theme, cycleTheme, setTheme } = useThemeStore();
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);
  const activeTheme = THEME_OPTIONS.find((option) => option.value === theme) ?? THEME_OPTIONS[0];
  const isLight = theme === 'light';

  useEffect(() => {
    function handlePointerDown(event: PointerEvent) {
      if (!containerRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') setOpen(false);
    }

    document.addEventListener('pointerdown', handlePointerDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, []);

  const chooseTheme = (value: ThemeMode) => {
    setTheme(value);
    setOpen(false);
  };

  return (
    <div ref={containerRef} className={cn('theme-switcher relative inline-flex', className)} title={`Thème actuel : ${activeTheme.label}`}>
      <div className="theme-toggle-shell inline-flex items-center overflow-hidden rounded-full border text-sm font-semibold shadow-sm backdrop-blur">
        <button
          type="button"
          onClick={cycleTheme}
          className="button-press inline-flex items-center gap-2 px-3 py-2 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2"
          aria-label={`Passer au thème suivant. Thème actuel : ${activeTheme.label}`}
        >
          {isLight ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
          <Palette className="h-4 w-4 opacity-75" />
        </button>
        <button
          type="button"
          onClick={() => setOpen((value) => !value)}
          className="button-press inline-flex min-w-28 items-center justify-between gap-2 border-l px-3 py-2 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2"
          aria-haspopup="listbox"
          aria-expanded={open}
          aria-label={`Choisir un thème Care Health. Thème actuel : ${activeTheme.label}`}
        >
          <span className="inline-flex items-center gap-2">
            <span className="theme-preview-dot" style={{ background: themePreview[theme] }} />
            <span>{activeTheme.shortLabel}</span>
          </span>
          <ChevronDown className={cn('h-4 w-4 transition-transform', open && 'rotate-180')} />
        </button>
      </div>

      {open && (
        <div className="theme-menu absolute right-0 top-full z-50 mt-2 w-64 overflow-hidden rounded-2xl border p-2 shadow-2xl" role="listbox" aria-label="Thèmes Care Health">
          <div className="px-3 py-2 text-xs font-bold uppercase tracking-[0.18em]">Palette d'interface</div>
          <div className="grid gap-1">
            {THEME_OPTIONS.map((option) => {
              const selected = option.value === theme;
              return (
                <button
                  key={option.value}
                  type="button"
                  role="option"
                  aria-selected={selected}
                  onClick={() => chooseTheme(option.value)}
                  className={cn('theme-menu-item button-press flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left text-sm font-semibold transition', selected && 'is-selected')}
                >
                  <span className="theme-preview-dot h-5 w-5" style={{ background: themePreview[option.value] }} />
                  <span className="min-w-0 flex-1">
                    <span className="block truncate">{option.label}</span>
                    <span className="block text-xs font-medium opacity-70">{option.shortLabel}</span>
                  </span>
                  {selected && <Check className="h-4 w-4" />}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}
