import { AlertTriangle } from 'lucide-react';

export function ErrorMessage({ message }: { message: string }) {
  return (
    <div className="flex items-start gap-3 rounded-xl border border-red-100 bg-red-50 p-4 text-sm text-red-700">
      <AlertTriangle className="mt-0.5 h-5 w-5 flex-none" />
      <span>{message}</span>
    </div>
  );
}
