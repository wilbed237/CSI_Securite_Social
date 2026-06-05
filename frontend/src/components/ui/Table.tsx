import type { ReactNode } from 'react';

export function DataTable({ headers, children, empty }: { headers: string[]; children?: ReactNode; empty?: string }) {
  return (
    <div className="care-table overflow-hidden rounded-2xl border shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead className="care-table-head">
            <tr>
              {headers.map((header) => (
                <th key={header} className="care-table-th px-4 py-3 text-left font-semibold">{header}</th>
              ))}
            </tr>
          </thead>
          <tbody className="care-table-body">
            {children ?? (
              <tr>
                <td colSpan={headers.length} className="care-table-empty px-4 py-8 text-center">{empty ?? 'Aucune donnée disponible.'}</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
