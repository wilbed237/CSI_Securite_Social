import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { InsuredDetailPage } from './InsuredDetailPage';
import { profileApi } from '../../api/profileApi';
import { useAuthStore } from '../../store/authStore';

vi.mock('../../api/profileApi', () => ({
  profileApi: {
    getInsured: vi.fn(),
    primaryDoctorHistory: vi.fn(),
    assignTreatingDoctor: vi.fn(),
  },
}));

vi.mock('../../hooks/usePageTitle', () => ({
  usePageTitle: () => undefined,
}));

describe('InsuredDetailPage', () => {
  beforeEach(() => {
    useAuthStore.setState({ user: { roles: ['AGENT'] } as never, accessToken: 'token', refreshToken: 'refresh' } as never);
    vi.mocked(profileApi.getInsured).mockResolvedValue({
      insuranceNumber: 'ASS-0002',
      firstName: 'Jane',
      lastName: 'Doe',
      birthDate: '1990-01-01',
      address: 'Yaoundé',
      phoneNumber: '650000000',
      email: 'jane@example.com',
      countryCode: 'CM',
      preferredPaymentType: 'CASH',
      status: 'ACTIVE',
    });
    vi.mocked(profileApi.primaryDoctorHistory).mockResolvedValue([]);
  });

  it('offers a clear action to assign a treating doctor later', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    render(
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/app/insured/ASS-0002']}>
          <Routes>
            <Route path="/app/insured/:insuranceNumber" element={<InsuredDetailPage />} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>,
    );

    expect(await screen.findByRole('button', { name: /assigner un médecin traitant/i })).toBeInTheDocument();
  });
});
