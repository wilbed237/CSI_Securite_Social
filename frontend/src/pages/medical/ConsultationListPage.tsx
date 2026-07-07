import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { consultationApi } from '../../api/consultationApi';
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
import { useAuthStore } from '../../store/authStore';
import type { ConsultationFilters, ConsultationStatus } from '../../types/api';

export function ConsultationListPage() {
  const { t } = useTranslation();
  usePageTitle(t('consultation.list.title'));
  const [params, setParams] = useSearchParams();
  const filters: ConsultationFilters = {
    page: Number(params.get('page') ?? 0), size: 20,
    sort: params.get('sort') ?? 'startedAt', direction: (params.get('direction') as 'asc' | 'desc') ?? 'desc',
    search: params.get('search') || undefined, patientId: params.get('patientId') || undefined,
    doctorId: params.get('doctorId') || undefined, status: (params.get('status') as ConsultationStatus) || undefined,
  };
  const query = useQuery({ queryKey: ['consultations', filters], queryFn: () => consultationApi.getConsultations(filters) });
  const canCreateConsultation = useAuthStore((state) => state.hasAnyRole(['DOCTOR', 'GENERALIST', 'SPECIALIST']));
  const set = (key: string, value: string) => setParams((current) => { const next = new URLSearchParams(current); if (value) next.set(key, value); else next.delete(key); if (key !== 'page') next.set('page', '0'); return next; });

  return <div>
    <PageHeader title={t('consultation.list.title')} description={t('consultation.list.description')} />
    <Card className="mb-5">
      <div className="grid gap-3 md:grid-cols-4">
        <Input label={t('common.search')} value={filters.search ?? ''} placeholder={t('consultation.searchId')} onChange={(e) => set('search', e.target.value)} />
        <Input label={t('diseaseSheet.patient')} value={filters.patientId ?? ''} onChange={(e) => set('patientId', e.target.value)} />
        <Select label={t('common.status')} value={filters.status ?? ''} onChange={(e) => set('status', e.target.value)} options={[{ label: t('common.all'), value: '' }, ...['DRAFT','IN_PROGRESS','COMPLETED','CANCELLED','ARCHIVED'].map((value) => ({ label: t(`status.${value}`, value), value }))]} />
        <Select label={t('common.sort')} value={`${filters.sort}:${filters.direction}`} onChange={(e) => { const [sort, direction] = e.target.value.split(':'); setParams((current) => { const next = new URLSearchParams(current); next.set('sort', sort); next.set('direction', direction); next.set('page', '0'); return next; }); }} options={[
          { label: t('medical.sort.dateDesc'), value: 'startedAt:desc' }, { label: t('medical.sort.dateAsc'), value: 'startedAt:asc' },
          { label: t('medical.sort.amountDesc'), value: 'cost:desc' }, { label: t('medical.sort.status'), value: 'status:asc' },
        ]} />
      </div>
      <div className="mt-4 flex gap-2"><Button variant="secondary" onClick={() => query.refetch()}>{t('common.refresh')}</Button>{canCreateConsultation ? <Link className="care-btn-primary rounded-xl px-4 py-2.5 text-sm font-semibold" to="/app/consultations/new">{t('common.create')}</Link> : null}</div>
    </Card>
    {query.isLoading ? <div className="grid gap-3"><SkeletonCard /><SkeletonCard /></div> : query.isError ? <ErrorState message={extractApiError(query.error)} /> : <>
      <DataTable headers={[t('common.id'), t('diseaseSheet.patient'), t('diseaseSheet.doctor'), t('doctor.type'), t('common.date'), t('consultation.reason'), t('common.amount'), t('common.status'), t('common.actions')]} empty={t('consultation.empty')}>
        {query.data?.content.length ? query.data.content.map((item) => <tr key={item.id}>
          <td className="px-4 py-3"><CopyIdButton id={item.id} /></td><td className="px-4 py-3">{item.insuranceNumber}</td><td className="px-4 py-3">{item.doctorMatricule}</td><td className="px-4 py-3">{t(`technical.${item.doctorType}`)}</td>
          <td className="px-4 py-3">{new Date(item.startedAt).toLocaleString()}</td><td className="max-w-48 truncate px-4 py-3">{item.reason}</td><td className="px-4 py-3">{item.cost.toLocaleString()} XAF</td><td className="px-4 py-3"><Badge>{t(`status.${item.status}`, item.status)}</Badge></td>
          <td className="px-4 py-3"><div className="flex gap-2"><Link className="text-primary-700 underline" to={`/app/consultations/${item.id}`}>{t('common.details')}</Link><Link className="text-primary-700 underline" to={`/app/consultations/${item.id}/edit`}>{t('common.edit')}</Link></div></td>
        </tr>) : undefined}
      </DataTable>
      <Pagination page={query.data?.number ?? 0} totalPages={query.data?.totalPages ?? 0} onPageChange={(page) => set('page', String(page))} />
    </>}
  </div>;
}
