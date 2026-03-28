import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { RotateCcw, XCircle, CheckCircle, Clock, AlertCircle, Package } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { getMyCancels, CANCEL_REASON_LABELS } from '../../api/services/cancel';
import type { CancelResponse, CancelReason } from '../../api/services/cancel';
import { getMyRefunds } from '../../api/services/refund';
import type { RefundResponse } from '../../api/services/refund';

type TabType = 'cancel' | 'refund';

export default function CancelRefundView() {
    const { t } = useTranslation();
    const [tab, setTab] = useState<TabType>('cancel');

    const CANCEL_STATUS_MAP = useMemo(
        () => ({
            REQUESTED: { label: t('cancelRefund.cancel_status_REQUESTED'), color: 'text-yellow-600 bg-yellow-50', icon: Clock },
            APPROVED: { label: t('cancelRefund.cancel_status_APPROVED'), color: 'text-blue-600 bg-blue-50', icon: CheckCircle },
            REJECTED: { label: t('cancelRefund.cancel_status_REJECTED'), color: 'text-red-600 bg-red-50', icon: XCircle },
            COMPLETED: { label: t('cancelRefund.cancel_status_COMPLETED'), color: 'text-green-600 bg-green-50', icon: CheckCircle },
        }),
        [t]
    );

    const REFUND_STATUS_MAP = useMemo(
        () => ({
            PENDING: { label: t('cancelRefund.refund_status_PENDING'), color: 'text-yellow-600 bg-yellow-50', icon: Clock },
            COMPLETED: { label: t('cancelRefund.refund_status_COMPLETED'), color: 'text-green-600 bg-green-50', icon: CheckCircle },
            FAILED: { label: t('cancelRefund.refund_status_FAILED'), color: 'text-red-600 bg-red-50', icon: AlertCircle },
        }),
        [t]
    );

    const { data: cancelsPage, isLoading: loadingCancels } = useQuery({
        queryKey: ['cancels'],
        queryFn: () => getMyCancels(0, 20),
    });

    const { data: refundsPage, isLoading: loadingRefunds } = useQuery({
        queryKey: ['refunds'],
        queryFn: () => getMyRefunds(0, 20),
    });

    const cancels = cancelsPage?.content || [];
    const refunds = refundsPage?.content || [];
    const isLoading = loadingCancels || loadingRefunds;

    const cancelReasonLabel = (reason: string) => {
        const key = `cancelReason.${reason}` as const;
        const tr = t(key);
        if (tr !== key) return tr;
        return CANCEL_REASON_LABELS[reason as CancelReason] || reason;
    };

    return (
        <div className="max-w-5xl mx-auto px-6 py-12 space-y-8">
            <div className="grid grid-cols-2 gap-4">
                <div className="bg-orange-50 p-5 rounded-2xl border border-orange-100 text-center">
                    <p className="text-xs font-bold text-orange-400 uppercase mb-1">{t('cancelRefund.stats_cancel')}</p>
                    <p className="text-2xl font-bold text-orange-600">
                        {cancels.length}
                        <span className="text-sm text-orange-300 ml-1">{t('common.count_suffix_items')}</span>
                    </p>
                </div>
                <div className="bg-blue-50 p-5 rounded-2xl border border-blue-100 text-center">
                    <p className="text-xs font-bold text-blue-400 uppercase mb-1">{t('cancelRefund.stats_refund')}</p>
                    <p className="text-2xl font-bold text-blue-600">
                        {refunds.length}
                        <span className="text-sm text-blue-300 ml-1">{t('common.count_suffix_items')}</span>
                    </p>
                </div>
            </div>

            <div className="flex space-x-2">
                <button
                    onClick={() => setTab('cancel')}
                    className={`flex items-center space-x-2 px-5 py-2.5 rounded-full text-xs font-bold transition-all ${tab === 'cancel' ? 'bg-black text-white' : 'bg-stone-100 text-stone-400 hover:bg-stone-200'
                        }`}
                >
                    <XCircle size={14} />
                    <span>{t('cancelRefund.tab_cancel')}</span>
                </button>
                <button
                    onClick={() => setTab('refund')}
                    className={`flex items-center space-x-2 px-5 py-2.5 rounded-full text-xs font-bold transition-all ${tab === 'refund' ? 'bg-black text-white' : 'bg-stone-100 text-stone-400 hover:bg-stone-200'
                        }`}
                >
                    <RotateCcw size={14} />
                    <span>{t('cancelRefund.tab_refund')}</span>
                </button>
            </div>

            {isLoading ? (
                <div className="flex justify-center py-10">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
                </div>
            ) : tab === 'cancel' ? (
                cancels.length === 0 ? (
                    <div className="text-center py-12">
                        <Package size={32} className="mx-auto mb-4 text-stone-200" />
                        <p className="text-stone-400 text-sm">{t('cancelRefund.empty_cancel')}</p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {cancels.map((cancel: CancelResponse) => {
                            const statusInfo = CANCEL_STATUS_MAP[cancel.status as keyof typeof CANCEL_STATUS_MAP] || {
                                label: cancel.status,
                                color: 'text-stone-500 bg-stone-50',
                                icon: Clock,
                            };
                            const StatusIcon = statusInfo.icon;
                            return (
                                <div key={cancel.id} className="bg-white rounded-2xl border border-stone-100 p-5 hover:border-stone-300 transition-all">
                                    <div className="flex items-start justify-between mb-3">
                                        <div>
                                            <p className="text-xs font-bold text-stone-400 uppercase tracking-wider">
                                                {t('cancelRefund.order_ref', { id: cancel.orderId })}
                                            </p>
                                            <p className="text-[10px] text-stone-300 mt-0.5">{cancel.createdAt?.split('T')[0]}</p>
                                        </div>
                                        <span className={`inline-flex items-center text-xs font-bold px-3 py-1 rounded-full ${statusInfo.color}`}>
                                            <StatusIcon size={12} className="mr-1" />
                                            {statusInfo.label}
                                        </span>
                                    </div>
                                    <p className="text-sm">
                                        <span className="text-stone-400">{t('cancelRefund.reason_prefix')}</span>{' '}
                                        {cancelReasonLabel(cancel.cancelReason)}
                                    </p>
                                    {cancel.rejectedReason && (
                                        <p className="text-xs text-red-400 mt-2 bg-red-50 p-2 rounded-lg">
                                            {t('cancelRefund.reject_prefix')} {cancel.rejectedReason}
                                        </p>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                )
            ) : (
                refunds.length === 0 ? (
                    <div className="text-center py-12">
                        <RotateCcw size={32} className="mx-auto mb-4 text-stone-200" />
                        <p className="text-stone-400 text-sm">{t('cancelRefund.empty_refund')}</p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {refunds.map((refund: RefundResponse) => {
                            const statusInfo = REFUND_STATUS_MAP[refund.status as keyof typeof REFUND_STATUS_MAP] || {
                                label: refund.status,
                                color: 'text-stone-500 bg-stone-50',
                                icon: Clock,
                            };
                            const StatusIcon = statusInfo.icon;
                            return (
                                <div key={refund.id} className="bg-white rounded-2xl border border-stone-100 p-5 hover:border-stone-300 transition-all">
                                    <div className="flex items-start justify-between mb-3">
                                        <div>
                                            <p className="text-xs font-bold text-stone-400 uppercase tracking-wider">
                                                {t('cancelRefund.order_ref', { id: refund.orderId })}
                                            </p>
                                            <p className="text-[10px] text-stone-300 mt-0.5">{refund.createdAt?.split('T')[0]}</p>
                                        </div>
                                        <span className={`inline-flex items-center text-xs font-bold px-3 py-1 rounded-full ${statusInfo.color}`}>
                                            <StatusIcon size={12} className="mr-1" />
                                            {statusInfo.label}
                                        </span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <p className="text-sm text-stone-400">
                                            {t('cancelRefund.refund_method')}{' '}
                                            {refund.refundMethod || t('cancelRefund.refund_method_default')}
                                        </p>
                                        <p className="font-bold">
                                            {refund.refundAmount?.toLocaleString()}
                                            {t('common.currency_won')}
                                        </p>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )
            )}
        </div>
    );
}
