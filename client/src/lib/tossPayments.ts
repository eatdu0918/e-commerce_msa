// 토스페이먼츠 SDK 싱글톤 초기화 모듈
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';

const TOSS_CLIENT_KEY = 'test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm';

let tossPaymentsInstance: Awaited<ReturnType<typeof loadTossPayments>> | null = null;

export async function getTossPayments() {
    if (!tossPaymentsInstance) {
        tossPaymentsInstance = await loadTossPayments(TOSS_CLIENT_KEY);
    }
    return tossPaymentsInstance;
}

export { TOSS_CLIENT_KEY };
