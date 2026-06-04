import type { ReactNode } from 'react';
import { useAuthStore } from '../store/authStore';
import type { RoleName } from '../types/api';

export function RoleGate({ roles, children, fallback = null }: { roles: RoleName[]; children: ReactNode; fallback?: ReactNode }) {
  return useAuthStore((state) => state.hasAnyRole)(roles) ? children : fallback;
}
