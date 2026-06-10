import { Check, Copy } from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';
import { useTranslation } from '../../i18n';
import { cn } from '../../utils/cn';

async function copyText(value: string): Promise<void> {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(value);
    return;
  }
  const textarea = document.createElement('textarea');
  textarea.value = value;
  textarea.style.position = 'fixed';
  textarea.style.opacity = '0';
  document.body.appendChild(textarea);
  textarea.select();
  const copied = document.execCommand('copy');
  textarea.remove();
  if (!copied) throw new Error('Clipboard unavailable');
}

export function CopyIdButton({ id, showValue = true, className }: { id: string; showValue?: boolean; className?: string }) {
  const { t } = useTranslation();
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    try {
      await copyText(id.trim());
      setCopied(true);
      toast.success(t('copyId.copied'));
      window.setTimeout(() => setCopied(false), 1800);
    } catch {
      toast.error(t('copyId.error'));
    }
  };

  return (
    <span className={cn('inline-flex min-w-0 items-center gap-1.5', className)}>
      {showValue && <span className="max-w-40 truncate font-mono text-xs" title={id}>{id}</span>}
      <button
        type="button"
        onClick={copy}
        title={copied ? t('copyId.copied') : t('copyId.copy')}
        aria-label={copied ? t('copyId.copied') : t('copyId.copy')}
        className="inline-flex h-8 w-8 flex-none items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-600 transition hover:border-primary-400 hover:text-primary-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 active:scale-95"
      >
        {copied ? <Check className="h-4 w-4 text-emerald-600" /> : <Copy className="h-4 w-4" />}
      </button>
    </span>
  );
}
