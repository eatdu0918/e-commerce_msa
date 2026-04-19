import axios from 'axios';
import i18n from '../i18n';
import {
    AUTH_STORAGE_KEYS,
    clearAuthStorage,
    getStoredAccessToken,
    getStoredRefreshToken,
} from '../lib/authStorage';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || '',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

let isRefreshing = false;
let failedQueue: Array<{
    resolve: (token: string) => void;
    reject: (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
    failedQueue.forEach(({ resolve, reject }) => {
        if (token) {
            resolve(token);
        } else {
            reject(error);
        }
    });
    failedQueue = [];
};

/** 세션 만료 등으로 저장소를 비운 뒤, 어드민 영역이면 로그인 페이지로 보냅니다. */
function redirectAfterSessionCleared() {
    const path = window.location.pathname;
    if (path.startsWith('/admin') && path !== '/admin/login') {
        window.location.href = '/admin/login';
        return;
    }
    window.location.href = '/?login=true';
}

// Request interceptor to add the auth token header to requests
api.interceptors.request.use(
    (config) => {
        const token = getStoredAccessToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        const lang = i18n.language?.toLowerCase().startsWith('en') ? 'en-US' : 'ko-KR';
        config.headers['Accept-Language'] = lang;
        // ngrok 무료 호스트는 브라우저 경고 HTML을 반환할 수 있어 JSON API가 깨질 수 있음
        const base = import.meta.env.VITE_API_URL || '';
        const viaNgrok =
            (typeof base === 'string' && base.includes('ngrok')) ||
            (typeof window !== 'undefined' && window.location.hostname.includes('ngrok'));
        if (viaNgrok) {
            config.headers['ngrok-skip-browser-warning'] = 'true';
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor with token refresh logic
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            const reqUrl = String(originalRequest.url ?? '');
            if (reqUrl.includes('/api/auth/login')) {
                return Promise.reject(error);
            }

            const refreshToken = getStoredRefreshToken();

            if (!refreshToken) {
                // 게스트(토큰 없음)가 보호된 API를 호출해 401이 난 경우 → 전역 리다이렉트 금지
                // (관리자 로그인/일반 쇼핑몰 혼용 시 홈·상품 페이지가 통째로 튕기는 문제 방지)
                const hadAuthHeader =
                    !!originalRequest.headers?.Authorization &&
                    String(originalRequest.headers.Authorization).startsWith('Bearer ');
                if (hadAuthHeader) {
                    clearAuthStorage();
                    redirectAfterSessionCleared();
                }
                return Promise.reject(error);
            }

            if (isRefreshing) {
                return new Promise<string>((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then((token) => {
                    originalRequest.headers['Authorization'] = `Bearer ${token}`;
                    return api(originalRequest);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const response = await axios.post(
                    `${import.meta.env.VITE_API_URL || ''}/api/auth/refresh`,
                    { refreshToken },
                    { headers: { 'Content-Type': 'application/json' } }
                );

                const { accessToken, refreshToken: newRefreshToken } = response.data.data;
                localStorage.setItem(AUTH_STORAGE_KEYS.accessToken, accessToken);
                if (newRefreshToken) {
                    localStorage.setItem(AUTH_STORAGE_KEYS.refreshToken, newRefreshToken);
                }

                processQueue(null, accessToken);
                originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
                return api(originalRequest);
            } catch (refreshError) {
                processQueue(refreshError, null);
                clearAuthStorage();
                redirectAfterSessionCleared();
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default api;
