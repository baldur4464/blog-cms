import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import axios from 'axios';
import { authApi } from '../api/auth';
import { setAccessToken } from '../api/client';
import i18n from '../i18n';

interface AuthUser {
  userId: string;
  username: string;
  role: string;
  preferredLocale: string | null;
}

interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function decodeToken(token: string): AuthUser {
  const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
  const roles: string[] = payload.roles ?? [];
  const role = roles[0]?.replace('ROLE_', '') ?? 'AUTHOR';
  return { userId: payload.userId, username: payload.sub, role, preferredLocale: payload.preferredLocale ?? null };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Try to restore session via refresh token cookie on page load
    axios
      .post<{ accessToken: string }>('/api/auth/refresh', {}, { withCredentials: true })
      .then((res) => {
        setAccessToken(res.data.accessToken);
        setUser(decodeToken(res.data.accessToken));
      })
      .catch(() => {
        setUser(null);
      })
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    const handleExpired = () => {
      setUser(null);
      setAccessToken(null);
    };
    window.addEventListener('auth:expired', handleExpired);
    return () => window.removeEventListener('auth:expired', handleExpired);
  }, []);

  // UI-Sprache aus dem Konto anwenden (ADR-018 Kapitel 3): preferredLocale gewinnt nach Login/Refresh.
  useEffect(() => {
    if (user?.preferredLocale && user.preferredLocale !== i18n.resolvedLanguage) {
      i18n.changeLanguage(user.preferredLocale);
    }
  }, [user?.preferredLocale]);

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password });
    setAccessToken(res.accessToken);
    setUser(decodeToken(res.accessToken));
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } finally {
      setAccessToken(null);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
