import { useSearchParams, useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export default function PaymentFailPage() {
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const code = searchParams.get('code') || 'UNKNOWN';
    const message = searchParams.get('message') || t('paymentFlow.fail_default_msg');

    // 결제 실패 시 pending 데이터 정리
    localStorage.removeItem('pendingOrderData');

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f9f7f2]">
            <div className="bg-white p-10 rounded-3xl shadow-sm border border-stone-100 max-w-md w-full text-center">
                <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6">
                    <span className="text-3xl">❌</span>
                </div>
                <h2 className="text-xl font-bold mb-3">{t('paymentFlow.fail_page_title')}</h2>
                <p className="text-sm text-stone-500 mb-2">{message}</p>
                <p className="text-xs text-stone-400 mb-8">{t('paymentFlow.error_code', { code })}</p>
                <div className="space-y-3">
                    <button
                        onClick={() => navigate(-1)}
                        className="w-full bg-black text-white py-3.5 rounded-2xl text-sm font-bold hover:bg-stone-800 transition-all flex items-center justify-center gap-2"
                    >
                        <ChevronLeft size={16} />
                        {t('paymentFlow.retry_go_back')}
                    </button>
                    <button
                        onClick={() => navigate('/shop', { replace: true })}
                        className="w-full bg-stone-100 text-stone-600 py-3.5 rounded-2xl text-sm font-medium hover:bg-stone-200 transition-all"
                    >
                        {t('paymentFlow.fail_cta_shop')}
                    </button>
                </div>
            </div>
        </div>
    );
}
