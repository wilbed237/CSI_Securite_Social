import { create } from 'zustand';

export type ThemeMode = 'light' | 'dark-blue' | 'dark-orange' | 'dark-purple' | 'dark-black' | 'dark-emerald' | 'dark-cyan' | 'dark-rose';

const STORAGE_KEY = 'care-health-theme-mode';
const DARK_CLASSES: ThemeMode[] = ['dark-blue', 'dark-orange', 'dark-purple', 'dark-black', 'dark-emerald', 'dark-cyan', 'dark-rose'];
export const THEME_OPTIONS: { value: ThemeMode; label: string; shortLabel: string }[] = [
  { value: 'light', label: 'Clair clinique', shortLabel: 'Clair' },
  { value: 'dark-blue', label: 'Nuit bleutée', shortLabel: 'Bleu' },
  { value: 'dark-orange', label: 'Nuit ambrée', shortLabel: 'Orange' },
  { value: 'dark-purple', label: 'Nuit violacée', shortLabel: 'Violet' },
  { value: 'dark-black', label: 'Noir profond', shortLabel: 'Noir' },
  { value: 'dark-emerald', label: 'Bloc opératoire', shortLabel: 'Vert' },
  { value: 'dark-cyan', label: 'Néon médical', shortLabel: 'Cyan' },
  { value: 'dark-rose', label: 'Cardio nuit', shortLabel: 'Rose' },
];

function isThemeMode(value: string | null): value is ThemeMode {
  return value === 'light' || DARK_CLASSES.includes(value as ThemeMode);
}

function getPreferredTheme(): ThemeMode {
  if (typeof window === 'undefined') return 'light';
  const stored = window.localStorage.getItem(STORAGE_KEY);
  if (isThemeMode(stored)) return stored;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark-blue' : 'light';
}

function applyTheme(theme: ThemeMode) {
  if (typeof document === 'undefined') return;
  const root = document.documentElement;
  root.classList.remove('dark', ...DARK_CLASSES.map((mode) => `theme-${mode}`));
  if (theme !== 'light') {
    root.classList.add('dark', `theme-${theme}`);
  }
  root.dataset.theme = theme;
  root.style.colorScheme = theme === 'light' ? 'light' : 'dark';
}

interface ThemeState {
  theme: ThemeMode;
  setTheme: (theme: ThemeMode) => void;
  cycleTheme: () => void;
}

const initialTheme = getPreferredTheme();
applyTheme(initialTheme);

export const useThemeStore = create<ThemeState>((set, get) => ({
  theme: initialTheme,
  setTheme: (theme) => {
    window.localStorage.setItem(STORAGE_KEY, theme);
    applyTheme(theme);
    set({ theme });
  },
  cycleTheme: () => {
    const currentIndex = THEME_OPTIONS.findIndex((option) => option.value === get().theme);
    const nextTheme = THEME_OPTIONS[(currentIndex + 1) % THEME_OPTIONS.length].value;
    get().setTheme(nextTheme);
  },
}));

export function initializeTheme() {
  applyTheme(getPreferredTheme());
}
