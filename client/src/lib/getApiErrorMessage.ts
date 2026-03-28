import type { AxiosError } from 'axios';
import i18n from '../i18n';

type ApiErrorBody = {
    message?: string;
    error?: { errorCode?: string; errorMessage?: string };
};

/**
 * 게이트웨이/백엔드 ApiResponse 형식과 axios 기본 메시지를 통일해 표시용 문구로 반환합니다.
 */
export function getApiErrorMessage(error: unknown, fallback?: string): string {
    const defaultFallback = fallback ?? i18n.t('api.fallback');
    if (error && typeof error === 'object' && 'response' in error) {
        const ax = error as AxiosError<ApiErrorBody>;
        const data = ax.response?.data;
        const status = ax.response?.status;

        if (data && typeof data === 'object') {
            const msg = data.error?.errorMessage;
            if (typeof msg === 'string' && msg.trim()) {
                return msg.trim();
            }
            if (typeof data.message === 'string' && data.message.trim()) {
                return data.message.trim();
            }
        }

        if (typeof status === 'number') {
            return i18n.t('api.server_error', { status });
        }
    }

    if (error instanceof Error && error.message) {
        return error.message;
    }

    return defaultFallback;
}
