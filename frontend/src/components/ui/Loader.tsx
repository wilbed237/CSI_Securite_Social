export function Loader({ label = 'Chargement...' }: { label?: string }) {
  return (
    <div className="flex items-center gap-3 rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-600 shadow-sm">
      <span className="h-5 w-5 animate-spin rounded-full border-2 border-primary-700 border-t-transparent" />
      {label}
    </div>
  );
}

export function SkeletonCard() {
  return <div className="h-32 animate-pulse rounded-2xl bg-slate-100" />;
}
