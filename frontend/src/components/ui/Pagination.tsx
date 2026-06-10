import { Button } from './Button';
import { useTranslation } from '../../i18n';

export function Pagination({ page, totalPages, onPageChange }: { page: number; totalPages: number; onPageChange: (page: number) => void }) {
  const { t } = useTranslation();
  if (totalPages <= 1) return null;
  return (
    <div className="mt-4 flex items-center justify-between gap-3 text-sm">
      <Button variant="secondary" disabled={page <= 0} onClick={() => onPageChange(page - 1)}>{t('common.previous')}</Button>
      <span>{t('common.page')} {page + 1} / {totalPages}</span>
      <Button variant="secondary" disabled={page + 1 >= totalPages} onClick={() => onPageChange(page + 1)}>{t('common.next')}</Button>
    </div>
  );
}
