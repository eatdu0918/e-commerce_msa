/** 브라우저 탭 간 공유를 위해 인증 정보는 localStorage에 보관합니다. */
export const AUTH_STORAGE_KEYS = {
    accessToken: 'accessToken',
    refreshToken: 'refreshToken',
    user: 'user',
    role: 'role',
} as const;

export function getStoredAccessToken(): string | null {
    return localStorage.getItem(AUTH_STORAGE_KEYS.accessToken);
}

export function getStoredRefreshToken(): string | null {
    return localStorage.getItem(AUTH_STORAGE_KEYS.refreshToken);
}

export function clearAuthStorage(): void {
    localStorage.removeItem(AUTH_STORAGE_KEYS.accessToken);
    localStorage.removeItem(AUTH_STORAGE_KEYS.refreshToken);
    localStorage.removeItem(AUTH_STORAGE_KEYS.user);
    localStorage.removeItem(AUTH_STORAGE_KEYS.role);
}

export function isAuthStorageKey(key: string | null): boolean {
    if (key === null) return true;
    return Object.values(AUTH_STORAGE_KEYS).includes(key as (typeof AUTH_STORAGE_KEYS)[keyof typeof AUTH_STORAGE_KEYS]);
}

/** 이전 sessionStorage 기반 인증을 localStorage로 한 번만 이전합니다. */
export function migrateLegacySessionAuthToLocal(): void {
    if (getStoredAccessToken()) return;
    const access = sessionStorage.getItem(AUTH_STORAGE_KEYS.accessToken);
    if (!access) return;
    const refresh = sessionStorage.getItem(AUTH_STORAGE_KEYS.refreshToken);
    const user = sessionStorage.getItem(AUTH_STORAGE_KEYS.user);
    const role = sessionStorage.getItem(AUTH_STORAGE_KEYS.role);
    localStorage.setItem(AUTH_STORAGE_KEYS.accessToken, access);
    if (refresh) localStorage.setItem(AUTH_STORAGE_KEYS.refreshToken, refresh);
    if (user) localStorage.setItem(AUTH_STORAGE_KEYS.user, user);
    if (role) localStorage.setItem(AUTH_STORAGE_KEYS.role, role);
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.accessToken);
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.refreshToken);
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.user);
    sessionStorage.removeItem(AUTH_STORAGE_KEYS.role);
}
