import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { consultationApi } from '../../api/consultationApi';
import { extractApiError } from '../../api/httpClient';
import { DetailRow, RecordDetails } from '../../components/medical/RecordDetails';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/ui/Badge';
import { Card } from '../../components/ui/Card';
import { CopyIdButton } from '../../components/ui/CopyIdButton';
import { ErrorState } from '../../components/ui/ErrorState';
import { Loader } from '../../components/ui/Loader';
import { useTranslation } from '../../i18n';

export function ConsultationDetailPage(){const{id=''}=useParams();const{t}=useTranslation();const query=useQuery({queryKey:['consultation',id],queryFn:()=>consultationApi.getConsultationById(id),enabled:Boolean(id)});if(query.isLoading)return <Loader/>;if(query.isError)return <div className="space-y-3"><ErrorState message={extractApiError(query.error)}/><button className="text-primary-700 underline" onClick={()=>query.refetch()}>{t('common.retry')}</button></div>;const item=query.data!;return <div><PageHeader title={t('consultation.detail.title')} description="" action={<Link className="care-btn-primary rounded-xl px-4 py-2.5 text-sm font-semibold" to={`/app/consultations/${id}/edit`}>{t('common.edit')}</Link>}/><Card><RecordDetails><DetailRow label={t('common.id')}><CopyIdButton id={item.id}/></DetailRow><DetailRow label={t('common.status')}><Badge>{t(`status.${item.status}`,item.status)}</Badge></DetailRow><DetailRow label={t('diseaseSheet.patient')}>{item.insuranceNumber}</DetailRow><DetailRow label={t('diseaseSheet.doctor')}>{item.doctorMatricule}</DetailRow><DetailRow label={t('common.date')}>{new Date(item.startedAt).toLocaleString()}</DetailRow><DetailRow label={t('common.amount')}>{item.cost.toLocaleString()} XAF</DetailRow><DetailRow label={t('consultation.reason')}>{item.reason}</DetailRow><DetailRow label={t('consultation.observations')}>{item.observations}</DetailRow><DetailRow label={t('consultation.diagnosis')}>{item.diagnosis}</DetailRow><DetailRow label={t('consultation.conclusion')}>{item.conclusion}</DetailRow><DetailRow label="Version">{item.version}</DetailRow></RecordDetails></Card></div>}
