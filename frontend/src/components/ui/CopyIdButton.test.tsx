import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CopyIdButton } from './CopyIdButton';

vi.mock('react-hot-toast', () => ({ default: { success: vi.fn(), error: vi.fn() } }));

describe('CopyIdButton', () => {
  const id = '8c73a024-17b6-4f94-a987-91f26fc28e30';

  beforeEach(() => {
    Object.assign(navigator, { clipboard: { writeText: vi.fn().mockResolvedValue(undefined) } });
  });

  it('copie l identifiant complet affiche de facon tronquee', async () => {
    render(<CopyIdButton id={id} />);
    fireEvent.click(screen.getByRole('button'));
    await waitFor(() => expect(navigator.clipboard.writeText).toHaveBeenCalledWith(id));
    expect(screen.getByTitle(id)).toHaveClass('truncate');
  });

  it('est utilisable au clavier et affiche la confirmation', async () => {
    render(<CopyIdButton id={id} showValue={false} />);
    const button = screen.getByRole('button');
    button.focus();
    fireEvent.keyDown(button, { key: 'Enter' });
    fireEvent.click(button);
    await waitFor(() => expect(button.getAttribute('aria-label')).toMatch(/copi/i));
  });

  it('gere un echec du presse-papiers', async () => {
    navigator.clipboard.writeText = vi.fn().mockRejectedValue(new Error('denied'));
    render(<CopyIdButton id={id} />);
    fireEvent.click(screen.getByRole('button'));
    await waitFor(() => expect(navigator.clipboard.writeText).toHaveBeenCalled());
  });
});
