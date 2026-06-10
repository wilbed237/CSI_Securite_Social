import { zodResolver } from '@hookform/resolvers/zod';
import { Download, FilePlus, Send, Search, CheckCircle } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import toast from 'react-hot-toast';
import { z } from 'zod';
import { medicalApi } from '../../api/medicalApi';
import { extractApiError } from '../../api/httpClient';
import { PageHeader } from '../../components/PageHeader';
import { Badge } from '../../components/ui/Badge';
import { Button } from '../../components/ui/Button';
import { Card, CardHeader } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { usePageTitle } from '../../hooks/usePageTitle';
import { useTranslation } from '../../i18n';
import type { DiseaseSheetResponse } from '../../types/api';
import { RoleGate } from '../../components/RoleGate';
import { CopyIdButton } from '../../components/ui/CopyIdButton';

const createSchema = z.object({
  consultationId: z.string().uuid('ID consultation invalide'),
  diagnosis: z.string().min(3, 'Diagnostic requis'),
});
const finalizeSchema = z.object({
  reimbursementNumber: z.string().min(1, 'Numéro de remboursement requis'),
  comment: z.string().optional(),
});
type CreateValues = z.infer<typeof createSchema>;
type FinalizeValues = z.infer<typeof finalizeSchema>;

function statusTone(status: string): 'success' | 'warning' | 'danger' | 'primary' | 'neutral' {
  switch (status) {
    case 'COMPLETED': case 'PAID': case 'APPROVED': return 'success';
    case 'ISSUED': case 'SUBMITTED': case 'UNDER_REVIEW': return 'warning';
    case 'REJECTED': case 'CANCELLED': return 'danger';
    default: return 'neutral';
  }
}

export function DiseaseSheetPage() {
  const { t } = useTranslation();
  usePageTitle(t('diseaseSheet.title'));
  const [sheetNumber, setSheetNumber] = useState('');
  const [sheet, setSheet] = useState<DiseaseSheetResponse | null>(null);
  const [showFinalize, setShowFinalize] = useState(false);
  const form = useForm<CreateValues>({ resolver: zodResolver(createSchema) });
  const finalizeForm = useForm<FinalizeValues>({ resolver: zodResolver(finalizeSchema) });

  const create = async (values: CreateValues) => {
    try {
      const result = await medicalApi.createDiseaseSheet(values);
      setSheet(result);
      setSheetNumber(result.sheetNumber);
      toast.success(t('diseaseSheet.created'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const search = async () => {
    try {
      const result = await medicalApi.getDiseaseSheet(sheetNumber);
      setSheet(result);
      toast.success(t('diseaseSheet.found'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const submitSheet = async () => {
    if (!sheet) return;
    try {
      const result = await medicalApi.submitDiseaseSheet(sheet.sheetNumber);
      setSheet(result);
      toast.success(t('diseaseSheet.submitted'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const completeSheet = async () => {
    if (!sheet) return;
    try {
      const result = await medicalApi.completeDiseaseSheet(sheet.sheetNumber, 'CASH');
      setSheet(result);
      toast.success(t('diseaseSheet.underReview'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const finalizeSheet = async (values: FinalizeValues) => {
    if (!sheet) return;
    try {
      const result = await medicalApi.finalizeDiseaseSheet(sheet.sheetNumber, values.reimbursementNumber, values.comment);
      setSheet(result);
      setShowFinalize(false);
      toast.success(t('diseaseSheet.finalized'));
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  const downloadPdf = async () => {
    if (!sheet) return;
    try {
      const blob = await medicalApi.downloadDiseaseSheetPdf(sheet.sheetNumber);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `feuille-maladie-${sheet.sheetNumber}.pdf`;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      toast.error(extractApiError(error));
    }
  };

  return (
    <div>
      <PageHeader title={t('diseaseSheet.title')} description={t('diseaseSheet.description')} />
      <div className="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
        <Card>
          <CardHeader title={t('diseaseSheet.create.title')} description={t('diseaseSheet.create.description')} />
          <form className="space-y-4" onSubmit={form.handleSubmit(create)}>
            <Input
              label={t('diseaseSheet.consultationId')}
              required
              error={form.formState.errors.consultationId?.message}
              {...form.register('consultationId')}
            />
            <Textarea
              label={t('diseaseSheet.diagnosis')}
              required
              error={form.formState.errors.diagnosis?.message}
              {...form.register('diagnosis')}
            />
            <Button isLoading={form.formState.isSubmitting} icon={<FilePlus className="h-4 w-4" />}>
              {t('diseaseSheet.create.button')}
            </Button>
          </form>
        </Card>

        <Card>
          <CardHeader title={t('diseaseSheet.search.title')} />
          <div className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <Input
              label={t('diseaseSheet.sheetNumber')}
              value={sheetNumber}
              onChange={(e) => setSheetNumber(e.target.value)}
            />
            <Button icon={<Search className="h-4 w-4" />} onClick={search}>
              {t('common.search')}
            </Button>
          </div>

          {sheet && (
            <div className="mt-5 rounded-xl bg-slate-50 p-4 text-sm space-y-1.5">
              <p><strong>ID :</strong> <CopyIdButton id={sheet.id} /></p>
              <p><strong>{t('common.reference')} :</strong> <CopyIdButton id={sheet.sheetNumber} /></p>
              <p><strong>{t('consultation.id')} :</strong> <CopyIdButton id={sheet.consultationId} /></p>
              <p><strong>{t('diseaseSheet.patient')} :</strong> {sheet.insuranceNumber}</p>
              <p><strong>{t('diseaseSheet.doctor')} :</strong> {sheet.doctorMatricule} ({t(`technical.${sheet.doctorType}`)})</p>
              <p><strong>{t('diseaseSheet.cost')} :</strong> {sheet.consultationCost != null ? new Intl.NumberFormat('fr-CM', { style: 'currency', currency: 'XAF', maximumFractionDigits: 0 }).format(Number(sheet.consultationCost)) : '—'}</p>
              <p><strong>{t('diseaseSheet.diagnosis')} :</strong> {sheet.diagnosis}</p>
              <div className="mt-2">
                <Badge tone={statusTone(sheet.status)}>{t(`status.${sheet.status}`, sheet.status)}</Badge>
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                <RoleGate roles={['DOCTOR', 'GENERALIST', 'SPECIALIST']}>
                  {sheet.status === 'ISSUED' && (
                    <Button icon={<Send className="h-4 w-4" />} onClick={submitSheet}>
                      {t('diseaseSheet.submit')}
                    </Button>
                  )}
                </RoleGate>

                <RoleGate roles={['AGENT', 'AGENT_SOCIAL', 'SOCIAL_AGENT', 'SECURITY_AGENT', 'ADMIN']}>
                  {sheet.status === 'SUBMITTED' && (
                    <Button onClick={completeSheet}>
                      {t('diseaseSheet.takeControl')}
                    </Button>
                  )}
                  {(sheet.status === 'APPROVED' || sheet.status === 'PAID' || sheet.status === 'UNDER_REVIEW') && (
                    <Button
                      variant="secondary"
                      icon={<CheckCircle className="h-4 w-4" />}
                      onClick={() => setShowFinalize(true)}
                    >
                      {t('diseaseSheet.finalize.button')}
                    </Button>
                  )}
                </RoleGate>

                <Button variant="secondary" icon={<Download className="h-4 w-4" />} onClick={downloadPdf}>
                  {t('diseaseSheet.downloadPdf')}
                </Button>
              </div>

              {showFinalize && (
                <div className="mt-4 rounded-lg border border-primary-200 bg-primary-50 p-4">
                  <p className="mb-3 text-xs font-semibold text-primary-700">
                    {t('diseaseSheet.finalize.description')}
                  </p>
                  <form className="space-y-3" onSubmit={finalizeForm.handleSubmit(finalizeSheet)}>
                    <Input
                      label={t('diseaseSheet.finalize.reimbursementNumber')}
                      required
                      error={finalizeForm.formState.errors.reimbursementNumber?.message}
                      {...finalizeForm.register('reimbursementNumber')}
                    />
                    <Input
                      label={t('diseaseSheet.finalize.comment')}
                      {...finalizeForm.register('comment')}
                    />
                    <div className="flex gap-2">
                      <Button isLoading={finalizeForm.formState.isSubmitting} icon={<CheckCircle className="h-4 w-4" />}>
                        {t('diseaseSheet.finalize.button')}
                      </Button>
                      <Button variant="secondary" onClick={() => setShowFinalize(false)}>
                        {t('common.cancel')}
                      </Button>
                    </div>
                  </form>
                </div>
              )}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
