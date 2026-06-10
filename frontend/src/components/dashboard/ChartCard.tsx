import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { Card, CardHeader } from '../ui/Card';

interface ChartPoint { label: string; value: number }

export function ChartCard({ title, points, valueSuffix = '', onPointClick }: { title: string; points: ChartPoint[]; valueSuffix?: string; onPointClick?: (point: ChartPoint) => void }) {
  return <Card className="h-full"><CardHeader title={title} />
    {points.length === 0 ? <p className="text-sm text-slate-500">Aucune donnée disponible.</p> : <>
      <div className="h-64" aria-hidden="true"><ResponsiveContainer width="100%" height="100%"><BarChart data={points} margin={{ top: 8, right: 8, left: 0, bottom: 8 }} onClick={(state) => { const payload = (state as { activePayload?: { payload?: ChartPoint }[] }).activePayload; const point = payload?.[0]?.payload; if (point) onPointClick?.(point); }}><CartesianGrid strokeDasharray="3 3" vertical={false} /><XAxis dataKey="label" tick={{ fontSize: 11 }} /><YAxis allowDecimals={false} tick={{ fontSize: 11 }} /><Tooltip formatter={(value) => `${String(value)}${valueSuffix}`} /><Bar dataKey="value" fill="#0f766e" radius={[4, 4, 0, 0]} className={onPointClick ? 'cursor-pointer' : ''} /></BarChart></ResponsiveContainer></div>
      <div className="mt-3 flex flex-wrap gap-2" aria-label={`${title} - données détaillées`}>{points.map((point) => <button type="button" key={point.label} disabled={!onPointClick} onClick={() => onPointClick?.(point)} className="rounded-md border border-slate-200 px-2 py-1 text-xs text-slate-600 transition enabled:hover:border-primary-400 enabled:hover:text-primary-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-primary-600">{point.label}: {point.value}{valueSuffix}</button>)}</div>
    </>}
  </Card>;
}
