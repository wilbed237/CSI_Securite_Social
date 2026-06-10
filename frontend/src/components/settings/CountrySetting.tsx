import { Globe2 } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { Select } from '../ui/Select';

const KEY = 'care-health-country';
const countries = [{ code: 'CM', label: 'Cameroun', currency: 'XAF', timezone: 'Africa/Douala' }, { code: 'FR', label: 'France', currency: 'EUR', timezone: 'Europe/Paris' }, { code: 'GB', label: 'United Kingdom', currency: 'GBP', timezone: 'Europe/London' }];

export function CountrySetting({ defaultCountry = 'CM' }: { defaultCountry?: string }) {
  const [country, setCountry] = useState(() => window.localStorage.getItem(KEY) ?? defaultCountry);
  const selected = countries.find((item) => item.code === country) ?? countries[0];
  const change = (value: string) => { window.localStorage.setItem(KEY, value); setCountry(value); toast.success('Pays de résidence enregistré'); };
  return <div className="rounded-lg border border-slate-200 p-4 transition hover:border-primary-300 focus-within:ring-2 focus-within:ring-primary-500"><div className="mb-3 flex items-center gap-3"><Globe2 className="h-5 w-5 text-primary-700" /><div><h3 className="font-bold text-slate-950">Pays de résidence</h3><p className="text-sm text-slate-500">{selected.label} · {selected.code} · {selected.currency} · {selected.timezone}</p></div></div><Select label="Pays" value={country} onChange={(event) => change(event.target.value)} options={countries.map((item) => ({ label: item.label, value: item.code }))} /></div>;
}
