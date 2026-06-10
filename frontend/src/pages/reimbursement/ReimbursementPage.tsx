import { zodResolver } from '@hookform/resolvers/zod';
import { CreditCard, Download, Search } from 'lucide-react';
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import { useForm, useWatch } from 'react-hook-form';
import toast from 'react-hot-toast';
import { z } from 'zod';
import { reimbursementApi } from '../../api/reimbursementApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { ConfirmModal } from '../../components/ui/ConfirmModal';
import { CopyIdButton } from '../../components/ui/CopyIdButton';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { DataTable } from '../../components/ui/Table';
import { ErrorState } from '../../components/ui/ErrorState';
import { Loader } from '../../components/ui/Loader';
import { useDisclosure } from '../../hooks/useDisclosure';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useTranslation } from '../../i18n';
import type { ReimbursementResponse, ReimbursementStatus, ReimbursementType } from '../../types/api';

const schema = z
  .object({
    sheetNumber: z.string().min(1, 'Numéro de feuille requis'),
    paymentType: z.enum(['CASH', 'BANK_TRANSFER']),
    bankIban: z.string().optional(),
  })
  .superRefine((value, ctx) => {
    if (value.paymentType === 'BANK_TRANSFER' && !value.bankIban) {
      ctx.addIssue({ code: 'custom', path: ['bankIban'], message: 'IBAN requis pour un virement' });
    }
  });
type FormValues = z.infer<typeof schema>;

function statusTone(status: string): 'success' | 'warning' | 'danger' | 'neutral' {
  if (status === 'EXECUTED') return 'success';
  if (status === 'REJECTED') return 'danger';
  return 'warning';
}

export function ReimbursementPage() {
  const { t } = useTranslation();
  usePageTitle(t('reimbursement.title'));
  const [last, setLast] = useState<ReimbursementResponse | null>(null);
  const [receiptLoading, setReceiptLoading] = useState(false);
  const [params, setParams] = useSearchParams();
  const [reference, setReference] = useState('');
  const modal = useDisclosure();
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { paymentType: 'CASH' },
  });
  const paymentType = useWatch({ control: form.control, name: 'paymentType' });
  const status = (params.get('status') as ReimbursementStatus) || '';
  const type = (params.get('type') as ReimbursementType) || '';
  const processedByCurrentUser = params.get('processedByCurrentUser') === 'true';
  const listQuery = useQuery({
    queryKey: ['reimbursements', status, type, processedByCurrentUser, params.get('startDate'), params.get('endDate')],
    queryFn: () =>
      reimbursementApi.list({
        status: status || undefined,
        type: type || undefined,
        startDate: params.get('startDate') || undefined,
        endDate: params.get('endDate') || undefined,
        processedByCurrentUser,
        page: 0,
        size: 50,
      }),
  });
  const setFilter = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    setParams(next, { replace: true });
  };
  const money = new Intl.NumberFormat('fr-CM', { style: 'currency', currency: 'XAF', maximumFractionDigits: 0 });

  const submit = async (values: FormValues) => {
    try {
      const result = await reimbursementApi.create(values);
      setLast(result);
      toast.success(t('reimbursement.created'));
      await listQuery.refetch();
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const act = async (action: 'approve' | 'execute' | 'reject') => {
    if (!last) return;
    try {
      const result =
        action === 'approve'
          ? await reimbursementApi.approve(last.reimbursementNumber)
          : action === 'execute'
            ? await reimbursementApi.execute(last.reimbursementNumber)
            : await reimbursementApi.reject(
                last.reimbursementNumber,
                window.prompt(t('reimbursement.reject.prompt')) || t('reimbursement.reject.default'),
              );
      setLast(result);
      await listQuery.refetch();
      toast.success(t('reimbursement.statusUpdated'));
    } catch (err) {
      toast.error(extractApiError(err));
    }
  };

  const search = async () => {
    try {
      const result = await reimbursementApi.get(reference);
      setLast(result);
      toast.success(t('reimbursement.found'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const downloadReceipt = async () => {
    if (!last) return;
    setReceiptLoading(true);
    try {
      const blob = await reimbursementApi.downloadReceipt(last.reimbursementNumber);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `justificatif-${last.reimbursementNumber}.pdf`;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error(t('reimbursement.receipt.unavailable'));
    } finally {
      setReceiptLoading(false);
    }
  };

  const canDownloadReceipt = last && last.status === 'EXECUTED';

  return (
    <div>
      <PageHeader title={t('reimbursement.title')} description={t('reimbursement.description')} />
      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        <Card>
          <CardHeader title={t('reimbursement.create.title')} description={t('reimbursement.create.description')} />
          <form className="space-y-4" onSubmit={form.handleSubmit(() => modal.openModal())}>
            <Input
              label={t('reimbursement.sheetNumber')}
              required
              error={form.formState.errors.sheetNumber?.message}
              {...form.register('sheetNumber')}
            />
            <Select
              label={t('reimbursement.paymentMethod')}
              required
              options={[
                { label: t('reimbursement.paymentMethod.CASH'), value: 'CASH' },
                { label: t('reimbursement.paymentMethod.BANK_TRANSFER'), value: 'BANK_TRANSFER' },
              ]}
              error={form.formState.errors.paymentType?.message}
              {...form.register('paymentType')}
            />
            {paymentType === 'BANK_TRANSFER' && (
              <Input
                label={t('reimbursement.iban')}
                required
                error={form.formState.errors.bankIban?.message}
                {...form.register('bankIban')}
              />
            )}
            <Button icon={<CreditCard className="h-4 w-4" />}>{t('reimbursement.calculateButton')}</Button>
          </form>
        </Card>

        <Card>
          <CardHeader title={t('reimbursement.search.title')} />
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <Input
              label={t('reimbursement.reference')}
              value={reference}
              onChange={(e) => setReference(e.target.value)}
            />
            <Button icon={<Search className="h-4 w-4" />} onClick={search}>
              {t('common.search')}
            </Button>
          </div>

          {last && (
            <div className="mt-5 rounded-xl bg-slate-50 p-4 text-sm space-y-1">
              <p><strong>ID :</strong> <CopyIdButton id={last.id} /></p>
              <p><strong>{t('common.reference')} :</strong> <CopyIdButton id={last.reimbursementNumber} /></p>
              <p><strong>{t('reimbursement.table.sheet')} :</strong> <CopyIdButton id={last.sheetNumber} /></p>
              <p><strong>Base :</strong> {money.format(last.baseAmount)}</p>
              <p><strong>Taux :</strong> {Number(last.rate) * 100} %</p>
              <p><strong>{t('common.amount')} :</strong> {money.format(last.reimbursedAmount)}</p>
              {last.paymentReference && (
                <p><strong>Réf. paiement :</strong> {last.paymentReference}</p>
              )}
              <div className="mt-2">
                <Badge tone={statusTone(last.status)}>{t(`status.${last.status}`, last.status)}</Badge>
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                {last.status === 'PENDING' && (
                  <>
                    <Button onClick={() => act('approve')}>{t('reimbursement.approve')}</Button>
                    <Button variant="secondary" onClick={() => act('reject')}>{t('reimbursement.reject')}</Button>
                  </>
                )}
                {last.status === 'APPROVED' && (
                  <Button onClick={() => act('execute')}>{t('reimbursement.execute')}</Button>
                )}
                {canDownloadReceipt && (
                  <Button
                    variant="secondary"
                    icon={<Download className="h-4 w-4" />}
                    isLoading={receiptLoading}
                    onClick={downloadReceipt}
                  >
                    {t('reimbursement.receipt')}
                  </Button>
                )}
              </div>
            </div>
          )}
        </Card>
      </div>

      <ConfirmModal
        open={modal.open}
        title={t('reimbursement.confirm.title')}
        message={t('reimbursement.confirm.message')}
        onClose={modal.closeModal}
        onConfirm={() => {
          modal.closeModal();
          form.handleSubmit(submit)();
        }}
      />

      <div className="mt-6">
        <Card>
          <CardHeader
            title={t('reimbursement.list.title')}
            description={processedByCurrentUser ? t('reimbursement.list.myFiles') : t('reimbursement.list.filtered')}
          />
          <div className="mb-4 grid gap-3 md:grid-cols-3">
            <Select
              label={t('reimbursement.filter.status')}
              value={status}
              onChange={(event) => setFilter('status', event.target.value)}
              options={[
                { label: t('common.all'), value: '' },
                { label: t('status.PENDING'), value: 'PENDING' },
                { label: t('status.APPROVED'), value: 'APPROVED' },
                { label: t('status.EXECUTED'), value: 'EXECUTED' },
                { label: t('status.REJECTED'), value: 'REJECTED' },
              ]}
            />
            <Select
              label={t('reimbursement.filter.type')}
              value={type}
              onChange={(event) => setFilter('type', event.target.value)}
              options={[
                { label: t('common.all'), value: '' },
                { label: t('type.CONSULTATION'), value: 'CONSULTATION' },
                { label: t('type.MEDICATION'), value: 'MEDICATION' },
                { label: t('type.HOSPITALIZATION'), value: 'HOSPITALIZATION' },
                { label: t('type.MEDICAL_EXAM'), value: 'MEDICAL_EXAM' },
                { label: t('type.IMAGING'), value: 'IMAGING' },
                { label: t('type.SURGERY'), value: 'SURGERY' },
                { label: t('type.SPECIALIZED_CARE'), value: 'SPECIALIZED_CARE' },
                { label: t('type.OTHER'), value: 'OTHER' },
              ]}
            />
            <Select
              label={t('reimbursement.filter.agent')}
              value={processedByCurrentUser ? 'current' : ''}
              onChange={(event) => setFilter('processedByCurrentUser', event.target.value === 'current' ? 'true' : '')}
              options={[
                { label: t('common.allAgents'), value: '' },
                { label: t('common.myFiles'), value: 'current' },
              ]}
            />
          </div>

          {listQuery.isLoading && <Loader />}
          {listQuery.isError && <ErrorState message={extractApiError(listQuery.error)} />}

          {listQuery.data && (
            <>
              <div className="mb-4 grid gap-3 sm:grid-cols-3">
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">{t('reimbursement.stats.total')}</p>
                  <p className="font-bold">
                    {money.format(listQuery.data.content.reduce((sum, item) => sum + item.reimbursedAmount, 0))}
                  </p>
                </div>
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">{t('reimbursement.stats.count')}</p>
                  <p className="font-bold">{listQuery.data.totalElements}</p>
                </div>
                <div className="rounded-lg bg-slate-50 p-3">
                  <p className="text-xs text-slate-500">{t('reimbursement.stats.average')}</p>
                  <p className="font-bold">
                    {money.format(
                      listQuery.data.content.length
                        ? listQuery.data.content.reduce((sum, item) => sum + item.reimbursedAmount, 0) /
                            listQuery.data.content.length
                        : 0,
                    )}
                  </p>
                </div>
              </div>

              <DataTable
                headers={[
                  t('reimbursement.table.reference'),
                  t('reimbursement.table.sheet'),
                  t('reimbursement.table.date'),
                  t('reimbursement.table.type'),
                  t('reimbursement.table.status'),
                  t('reimbursement.table.amount'),
                ]}
                empty={t('reimbursement.empty')}
              >
                {listQuery.data.content.map((item) => (
                  <tr key={item.id} className="care-table-row">
                    <td className="care-table-cell-strong px-4 py-3"><CopyIdButton id={item.reimbursementNumber} /></td>
                    <td className="care-table-cell px-4 py-3"><CopyIdButton id={item.sheetNumber} /></td>
                    <td className="care-table-cell px-4 py-3">{item.date}</td>
                    <td className="care-table-cell px-4 py-3">
                      {t(`type.${item.reimbursementType}`, item.reimbursementType.replaceAll('_', ' '))}
                    </td>
                    <td className="px-4 py-3">
                      <Badge tone={statusTone(item.status)}>
                        {t(`status.${item.status}`, item.status)}
                      </Badge>
                    </td>
                    <td className="care-table-cell px-4 py-3">{money.format(item.reimbursedAmount)}</td>
                  </tr>
                ))}
              </DataTable>
            </>
          )}
        </Card>
      </div>
    </div>
  );
}
