import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import type { RoleName } from '../types/api';

export function ProtectedRoute({ children, roles }: { children: ReactNode; roles?: RoleName[] }) {
  const location = useLocation();
  const accessToken = useAuthStore((state) => state.accessToken);
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);

  if (!accessToken) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles && !hasAnyRole(roles)) {
    return <Navigate to="/forbidden" replace />;
  }

  return children;
}
