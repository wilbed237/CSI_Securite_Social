import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { prescriptionApi } from '../../api/prescriptionApi';
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
import type { PrescriptionFilters, PrescriptionStatus } from '../../types/api';

export function PrescriptionListPage() {
  const { t } = useTranslation(); usePageTitle(t('prescription.list.title'));
  const [params, setParams] = useSearchParams();
  const filters: PrescriptionFilters = { page: Number(params.get('page') ?? 0), size: 20, sort: params.get('sort') ?? 'prescriptionDate', direction: (params.get('direction') as 'asc'|'desc') ?? 'desc', search: params.get('search') || undefined, medicationName: params.get('medicationName') || undefined, status: (params.get('status') as PrescriptionStatus) || undefined };
  const query = useQuery({ queryKey: ['prescriptions', filters], queryFn: () => prescriptionApi.getPrescriptions(filters) });
  const canCreatePrescription = useAuthStore((state) => state.hasAnyRole(['DOCTOR', 'GENERALIST', 'SPECIALIST']));
  const set = (key: string, value: string) => setParams((current) => { const next = new URLSearchParams(current); if (value) next.set(key, value); else next.delete(key); if (key !== 'page') next.set('page', '0'); return next; });
  return <div><PageHeader title={t('prescription.list.title')} description={t('prescription.list.description')} />
    <Card className="mb-5"><div className="grid gap-3 md:grid-cols-4"><Input label={t('common.search')} placeholder={t('prescription.searchId')} value={filters.search ?? ''} onChange={(e) => set('search', e.target.value)} /><Input label={t('prescription.medicationName')} value={filters.medicationName ?? ''} onChange={(e) => set('medicationName', e.target.value)} /><Select label={t('common.status')} value={filters.status ?? ''} onChange={(e) => set('status', e.target.value)} options={[{label:t('common.all'),value:''},...['DRAFT','ACTIVE','FINALIZED','CANCELLED'].map((value)=>({label:t(`status.${value}`,value),value}))]} /><Select label={t('common.sort')} value={`${filters.sort}:${filters.direction}`} onChange={(e)=>{const [sort,direction]=e.target.value.split(':');setParams((current)=>{const next=new URLSearchParams(current);next.set('sort',sort);next.set('direction',direction);return next;});}} options={[{label:t('medical.sort.dateDesc'),value:'prescriptionDate:desc'},{label:t('medical.sort.dateAsc'),value:'prescriptionDate:asc'},{label:t('medical.sort.status'),value:'status:asc'}]} /></div><div className="mt-4 flex gap-2"><Button variant="secondary" onClick={()=>query.refetch()}>{t('common.refresh')}</Button>{canCreatePrescription ? <Link className="care-btn-primary rounded-xl px-4 py-2.5 text-sm font-semibold" to="/app/prescriptions/new">{t('common.create')}</Link> : null}</div></Card>
    {query.isLoading?<div className="grid gap-3"><SkeletonCard/><SkeletonCard/></div>:query.isError?<ErrorState message={extractApiError(query.error)}/>:<><DataTable headers={[t('common.id'),t('consultation.id'),t('diseaseSheet.title'),t('diseaseSheet.patient'),t('diseaseSheet.doctor'),t('common.date'),t('prescription.medicationCount'),t('common.status'),t('common.actions')]} empty={t('prescription.empty')}>{query.data?.content.length?query.data.content.map((item)=><tr key={item.id}><td className="px-4 py-3"><CopyIdButton id={item.id}/></td><td className="px-4 py-3"><CopyIdButton id={item.consultationId}/></td><td className="px-4 py-3">{item.diseaseSheetId?<CopyIdButton id={item.diseaseSheetId}/>: '—'}</td><td className="px-4 py-3">{item.insuranceNumber}</td><td className="px-4 py-3">{item.doctorMatricule}</td><td className="px-4 py-3">{new Date(item.prescriptionDate).toLocaleDateString()}</td><td className="px-4 py-3">{item.medicationCount}</td><td className="px-4 py-3"><Badge>{t(`status.${item.status}`,item.status)}</Badge></td><td className="px-4 py-3"><div className="flex gap-2"><Link className="text-primary-700 underline" to={`/app/prescriptions/${item.id}`}>{t('common.details')}</Link><Link className="text-primary-700 underline" to={`/app/prescriptions/${item.id}/edit`}>{t('common.edit')}</Link></div></td></tr>):undefined}</DataTable><Pagination page={query.data?.number??0} totalPages={query.data?.totalPages??0} onPageChange={(page)=>set('page',String(page))}/></>}
  </div>;
}
