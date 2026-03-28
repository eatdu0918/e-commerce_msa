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

// Request interceptor to add the auth token header to requests
api.interceptors.request.use(
    (config) => {
        const token = getStoredAccessToken();
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        const lang = i18n.language?.toLowerCase().startsWith('en') ? 'en-US' : 'ko-KR';
        config.headers['Accept-Language'] = lang;
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
            const refreshToken = getStoredRefreshToken();

            if (!refreshToken) {
                clearAuthStorage();
                window.location.href = '/?login=true';
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
                window.location.href = '/?login=true';
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default api;
