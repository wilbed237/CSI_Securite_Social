import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';
import type { AuthResponse, RoleName, UserResponse } from '../types/api';

interface AuthState {
  accessToken?: string;
  refreshToken?: string;
  user?: UserResponse;
  setSession: (session: AuthResponse) => void;
  clearSession: () => void;
  hasAnyRole: (roles: RoleName[]) => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: undefined,
      refreshToken: undefined,
      user: undefined,
      setSession: (session) => set({ accessToken: session.accessToken, refreshToken: session.refreshToken, user: session.user }),
      clearSession: () => set({ accessToken: undefined, refreshToken: undefined, user: undefined }),
      hasAnyRole: (roles) => {
        const userRoles = get().user?.roles ?? [];
        return roles.some((role) => userRoles.includes(role));
      },
    }),
    {
      name: 'csi-auth-session',
      storage: createJSONStorage(() => sessionStorage),
      partialize: (state) => ({ accessToken: state.accessToken, refreshToken: state.refreshToken, user: state.user }),
    },
  ),
);
