import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { CreditCard, CheckCircle, Clock, AlertCircle, Wallet } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { getMyPayments } from '../../api/services/payment';
import type { PaymentResponse } from '../../api/services/payment';

export default function PaymentHistoryView() {
    const { t } = useTranslation();

    const PAYMENT_STATUS_MAP = useMemo(
        () => ({
            PENDING: { label: t('paymentHistory.status_PENDING'), color: 'text-yellow-600 bg-yellow-50', icon: Clock },
            COMPLETED: { label: t('paymentHistory.status_COMPLETED'), color: 'text-green-600 bg-green-50', icon: CheckCircle },
            FAILED: { label: t('paymentHistory.status_FAILED'), color: 'text-red-600 bg-red-50', icon: AlertCircle },
            CANCELLED: { label: t('paymentHistory.status_CANCELLED'), color: 'text-stone-500 bg-stone-50', icon: AlertCircle },
            REFUNDED: { label: t('paymentHistory.status_REFUNDED'), color: 'text-blue-600 bg-blue-50', icon: CheckCircle },
        }),
        [t]
    );

    const paymentMethodLabel = (method: string) => {
        const key = `paymentHistory.method_${method}` as const;
        const translated = t(key);
        return translated === key ? method : translated;
    };

    const { data: paymentsPage, isLoading } = useQuery({
        queryKey: ['payments'],
        queryFn: () => getMyPayments(0, 20),
    });

    const payments = paymentsPage?.content || [];

    if (isLoading) {
        return (
            <div className="flex justify-center py-10">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
            </div>
        );
    }

    if (payments.length === 0) {
        return (
            <div className="text-center py-12">
                <Wallet size={32} className="mx-auto mb-4 text-stone-200" />
                <p className="text-stone-400 text-sm">{t('paymentHistory.empty')}</p>
            </div>
        );
    }

    const completedSum = payments
        .filter((p: PaymentResponse) => p.status === 'COMPLETED')
        .reduce((sum: number, p: PaymentResponse) => sum + p.payAmount, 0);

    return (
        <div className="max-w-5xl mx-auto px-6 py-12 space-y-8">
            <div className="bg-stone-50 p-6 rounded-2xl border border-stone-100">
                <p className="text-xs font-bold text-stone-400 uppercase mb-2">{t('paymentHistory.total_count_label')}</p>
                <div className="flex items-end justify-between">
                    <p className="text-3xl font-bold">
                        {payments.length}
                        <span className="text-sm text-stone-400 ml-1">{t('common.count_suffix_items')}</span>
                    </p>
                    <p className="text-sm text-stone-400">
                        {t('paymentHistory.sum_completed', {
                            amount: `${completedSum.toLocaleString()}${t('common.currency_won')}`,
                        })}
                    </p>
                </div>
            </div>

            <div className="space-y-4">
                {payments.map((payment: PaymentResponse) => {
                    const statusInfo = PAYMENT_STATUS_MAP[payment.status as keyof typeof PAYMENT_STATUS_MAP] || {
                        label: payment.statusDescription || payment.status,
                        color: 'text-stone-500 bg-stone-50',
                        icon: Clock,
                    };
                    const StatusIcon = statusInfo.icon;

                    return (
                        <div key={payment.id} className="bg-white rounded-2xl border border-stone-100 p-5 hover:border-stone-300 transition-all">
                            <div className="flex items-start justify-between mb-3">
                                <div>
                                    <p className="text-xs font-bold text-stone-400 uppercase tracking-wider">
                                        {t('paymentHistory.order_ref', { id: payment.orderId })}
                                    </p>
                                    <p className="text-[10px] text-stone-300 mt-0.5">{payment.createdAt?.split('T')[0]}</p>
                                </div>
                                <span className={`inline-flex items-center text-xs font-bold px-3 py-1 rounded-full ${statusInfo.color}`}>
                                    <StatusIcon size={12} className="mr-1" />
                                    {statusInfo.label}
                                </span>
                            </div>
                            <div className="flex justify-between items-center">
                                <div className="flex items-center space-x-2 text-sm text-stone-400">
                                    <CreditCard size={14} />
                                    <span>{paymentMethodLabel(payment.paymentMethod)}</span>
                                </div>
                                <p className="font-bold text-lg">
                                    {payment.payAmount?.toLocaleString()}
                                    {t('common.currency_won')}
                                </p>
                            </div>
                            {payment.paidAt && (
                                <p className="text-[10px] text-stone-300 mt-2">
                                    {t('paymentHistory.paid_at', {
                                        datetime: payment.paidAt.replace('T', ' ').substring(0, 19),
                                    })}
                                </p>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
