import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { diseaseSheetApi } from '../../api/diseaseSheetApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { CopyIdButton } from '../../components/ui/CopyIdButton';
import { ErrorState } from '../../components/ui/ErrorState';
import { Input } from '../../components/ui/Input';
import { SkeletonCard } from '../../components/ui/Loader';
import { Pagination } from '../../components/ui/Pagination';
import { Select } from '../../components/ui/Select';
import { DataTable } from '../../components/ui/Table';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useTranslation } from '../../i18n';
import type { DiseaseSheetFilters, DiseaseSheetStatus } from '../../types/api';

export function DiseaseSheetListPage() {
  const { t } = useTranslation();
  usePageTitle(t('diseaseSheet.list.title'));
  const [params, setParams] = useSearchParams();
  const filters: DiseaseSheetFilters = {
    page: Number(params.get('page') ?? 0), size: 20,
    sort: params.get('sort') ?? 'consultationDate', direction: (params.get('direction') as 'asc' | 'desc') ?? 'desc',
    search: params.get('search') || undefined, status: (params.get('status') as DiseaseSheetStatus) || undefined,
    hasReimbursement: params.get('hasReimbursement') ? params.get('hasReimbursement') === 'true' : undefined,
  };
  const query = useQuery({ queryKey: ['disease-sheets', filters], queryFn: () => diseaseSheetApi.getDiseaseSheets(filters) });
  const set = (key: string, value: string) => setParams((current) => {
    const next = new URLSearchParams(current);
    if (value) next.set(key, value); else next.delete(key);
    if (key !== 'page') next.set('page', '0');
    return next;
  });

  return <div>
    <PageHeader title={t('diseaseSheet.list.title')} description={t('diseaseSheet.list.description')} />
    <Card className="mb-5">
      <div className="grid gap-3 md:grid-cols-4">
        <Input label={t('common.search')} placeholder={t('diseaseSheet.searchId')} value={filters.search ?? ''} onChange={(e) => set('search', e.target.value)} />
        <Select label={t('common.status')} value={filters.status ?? ''} onChange={(e) => set('status', e.target.value)} options={[{ label: t('common.all'), value: '' }, ...['DRAFT','ISSUED','SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED','PAID','COMPLETED','CANCELLED'].map((value) => ({ label: t(`status.${value}`, value), value }))]} />
        <Select label={t('diseaseSheet.reimbursement')} value={filters.hasReimbursement === undefined ? '' : String(filters.hasReimbursement)} onChange={(e) => set('hasReimbursement', e.target.value)} options={[{ label: t('common.all'), value: '' }, { label: t('common.yes'), value: 'true' }, { label: t('common.no'), value: 'false' }]} />
        <Select label={t('common.sort')} value={`${filters.sort}:${filters.direction}`} onChange={(e) => { const [sort, direction] = e.target.value.split(':'); setParams((current) => { const next = new URLSearchParams(current); next.set('sort', sort); next.set('direction', direction); return next; }); }} options={[{ label: t('medical.sort.dateDesc'), value: 'consultationDate:desc' }, { label: t('medical.sort.dateAsc'), value: 'consultationDate:asc' }, { label: t('medical.sort.amountDesc'), value: 'consultationAmount:desc' }, { label: t('medical.sort.status'), value: 'status:asc' }]} />
      </div>
      <div className="mt-4 flex gap-2"><Button variant="secondary" onClick={() => query.refetch()}>{t('common.refresh')}</Button><Link className="care-btn-primary rounded-xl px-4 py-2.5 text-sm font-semibold" to="/app/disease-sheets/new">{t('common.create')}</Link></div>
    </Card>
    {query.isLoading ? <div className="grid gap-3"><SkeletonCard /><SkeletonCard /></div> : query.isError ? <ErrorState message={extractApiError(query.error)} /> : <>
      <DataTable headers={[t('common.id'), t('diseaseSheet.patient'), t('diseaseSheet.doctor'), t('doctor.type'), t('consultation.id'), t('common.date'), t('common.amount'), t('common.status'), t('diseaseSheet.reimbursement'), t('common.actions')]} empty={t('diseaseSheet.empty')}>
        {query.data?.content.length ? query.data.content.map((item) => <tr key={item.id}>
          <td className="px-4 py-3"><CopyIdButton id={item.id} /></td><td className="px-4 py-3">{item.insuranceNumber}</td><td className="px-4 py-3">{item.doctorMatricule}</td><td className="px-4 py-3">{t(`technical.${item.doctorType}`)}</td><td className="px-4 py-3"><CopyIdButton id={item.consultationId} /></td><td className="px-4 py-3">{new Date(item.consultationDate).toLocaleDateString()}</td><td className="px-4 py-3">{item.consultationCost.toLocaleString()} XAF</td><td className="px-4 py-3"><Badge>{t(`status.${item.status}`, item.status)}</Badge></td><td className="px-4 py-3">{item.reimbursementId ? <CopyIdButton id={item.reimbursementId} /> : item.reimbursementNumber ?? '—'}</td><td className="px-4 py-3"><div className="flex gap-2"><Link className="text-primary-700 underline" to={`/app/disease-sheets/${item.id}`}>{t('common.details')}</Link><Link className="text-primary-700 underline" to={`/app/disease-sheets/${item.id}/edit`}>{t('common.edit')}</Link></div></td>
        </tr>) : undefined}
      </DataTable>
      <Pagination page={query.data?.number ?? 0} totalPages={query.data?.totalPages ?? 0} onPageChange={(page) => set('page', String(page))} />
    </>}
  </div>;
}
