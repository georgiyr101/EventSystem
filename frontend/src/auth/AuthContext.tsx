import React from "react";
import { authMe } from "../api/auth";
import type { AppRole, AuthResponseDto } from "../api/types";
import { clearAccessToken, getAccessToken, setAccessToken } from "./token";

export type AuthProfile = {
  userId: number;
  email: string;
  fullName: string;
  role: AppRole;
  organizerId: number | null;
};

function fromAuthDto(dto: AuthResponseDto): AuthProfile {
  return {
    userId: dto.userId,
    email: dto.email,
    fullName: dto.fullName,
    role: dto.role,
    organizerId: dto.organizerId,
  };
}

type AuthContextValue = {
  profile: AuthProfile | null;
  ready: boolean;
  setSession: (dto: AuthResponseDto) => void;
  logout: () => void;
  refreshMe: () => Promise<void>;
};

const AuthContext = React.createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [profile, setProfile] = React.useState<AuthProfile | null>(null);
  const [ready, setReady] = React.useState(false);

  const setSession = React.useCallback((dto: AuthResponseDto) => {
    setAccessToken(dto.accessToken);
    setProfile(fromAuthDto(dto));
  }, []);

  const logout = React.useCallback(() => {
    clearAccessToken();
    setProfile(null);
  }, []);

  const refreshMe = React.useCallback(async () => {
    const token = getAccessToken();
    if (!token) {
      setProfile(null);
      return;
    }
    const dto = await authMe();
    setSession(dto);
  }, [setSession]);

  React.useEffect(() => {
    let cancelled = false;
    (async () => {
      const token = getAccessToken();
      if (!token) {
        if (!cancelled) setReady(true);
        return;
      }
      try {
        const dto = await authMe();
        if (!cancelled) {
          setAccessToken(dto.accessToken);
          setProfile(fromAuthDto(dto));
        }
      } catch {
        if (!cancelled) {
          clearAccessToken();
          setProfile(null);
        }
      } finally {
        if (!cancelled) setReady(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const value = React.useMemo(
    () => ({ profile, ready, setSession, logout, refreshMe }),
    [profile, ready, setSession, logout, refreshMe],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = React.useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
