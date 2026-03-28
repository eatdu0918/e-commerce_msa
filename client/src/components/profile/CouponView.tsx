import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Ticket, Tag, Clock, CheckCircle, XCircle, Gift } from 'lucide-react';
import { getMyCoupons, getAvailableCoupons, claimCoupon } from '../../api/services/coupon';
import type { UserCouponResponse } from '../../api/services/coupon';
import { useTranslation } from 'react-i18next';

type TabType = 'available' | 'all' | 'used';

export default function CouponView() {
    const { t } = useTranslation();
    const queryClient = useQueryClient();
    const [tab, setTab] = useState<TabType>('available');
    const [couponCode, setCouponCode] = useState('');
    const [claimError, setClaimError] = useState('');
    const [claimSuccess, setClaimSuccess] = useState('');

    const { data: allCoupons = [], isLoading: loadingAll } = useQuery({
        queryKey: ['coupons', 'all'],
        queryFn: getMyCoupons,
    });

    const { data: availableCoupons = [], isLoading: loadingAvail } = useQuery({
        queryKey: ['coupons', 'available'],
        queryFn: getAvailableCoupons,
    });

    const claimMutation = useMutation({
        mutationFn: (code: string) => claimCoupon(code),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['coupons'] });
            setCouponCode('');
            setClaimError('');
            setClaimSuccess(t('couponView.claim_success'));
            setTimeout(() => setClaimSuccess(''), 3000);
        },
        onError: (err: any) => {
            setClaimError(err.response?.data?.message || t('couponView.claim_fail'));
            setClaimSuccess('');
        },
    });

    const usedCoupons = allCoupons.filter((c: UserCouponResponse) => c.status === 'USED' || !c.isAvailable);
    const isLoading = loadingAll || loadingAvail;

    const getDisplayCoupons = (): UserCouponResponse[] => {
        switch (tab) {
            case 'available': return availableCoupons;
            case 'used': return usedCoupons;
            default: return allCoupons;
        }
    };

    const displayCoupons = getDisplayCoupons();

    const getDaysUntil = (dateStr: string): number => {
        const diff = new Date(dateStr).getTime() - new Date().getTime();
        return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
    };

    return (
        <div className="max-w-5xl mx-auto px-6 py-12 space-y-8">
            {/* Coupon Stats */}
            <div className="grid grid-cols-2 gap-4">
                <div className="bg-stone-50 p-6 rounded-2xl border border-stone-100 text-center">
                    <p className="text-xs font-bold text-stone-400 uppercase mb-1">{t('couponView.owned')}</p>
                    <p className="text-3xl font-bold">
                        {allCoupons.length}
                        {t('couponView.sheet_suffix') ? (
                            <span className="text-sm text-stone-400 ml-1">{t('couponView.sheet_suffix')}</span>
                        ) : null}
                    </p>
                </div>
                <div className="bg-blue-50 p-6 rounded-2xl border border-blue-100 text-center">
                    <p className="text-xs font-bold text-blue-400 uppercase mb-1">{t('couponView.available_tab_label')}</p>
                    <p className="text-3xl font-bold text-blue-600">
                        {availableCoupons.length}
                        {t('couponView.sheet_suffix') ? (
                            <span className="text-sm text-blue-300 ml-1">{t('couponView.sheet_suffix')}</span>
                        ) : null}
                    </p>
                </div>
            </div>

            {/* Coupon Code Input */}
            <div className="bg-stone-50 rounded-2xl p-5 border border-stone-100">
                <div className="flex space-x-3">
                    <input
                        type="text"
                        value={couponCode}
                        onChange={(e) => setCouponCode(e.target.value)}
                        placeholder={t('couponView.code_placeholder')}
                        className="flex-1 px-4 py-3 bg-white border border-stone-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-black/10"
                    />
                    <button
                        onClick={() => couponCode.trim() && claimMutation.mutate(couponCode.trim())}
                        disabled={!couponCode.trim() || claimMutation.isPending}
                        className="px-6 py-3 bg-black text-white rounded-xl text-sm font-bold hover:bg-stone-800 transition-all disabled:opacity-50"
                    >
                        {claimMutation.isPending ? t('couponView.registering') : t('couponView.register')}
                    </button>
                </div>
                {claimError && <p className="text-red-500 text-xs mt-2">{claimError}</p>}
                {claimSuccess && <p className="text-green-500 text-xs mt-2">{claimSuccess}</p>}
            </div>

            {/* Tabs */}
            <div className="flex space-x-2">
                {([
                    { key: 'available', labelKey: 'couponView.tab_available' },
                    { key: 'all', labelKey: 'couponView.tab_all' },
                    { key: 'used', labelKey: 'couponView.tab_used' },
                ] as const).map((tabDef) => (
                    <button
                        key={tabDef.key}
                        onClick={() => setTab(tabDef.key)}
                        className={`px-4 py-2 rounded-full text-xs font-bold transition-all ${tab === tabDef.key ? 'bg-black text-white' : 'bg-stone-100 text-stone-400 hover:bg-stone-200'
                            }`}
                    >
                        {t(tabDef.labelKey)}
                    </button>
                ))}
            </div>

            {/* Coupon List */}
            {isLoading ? (
                <div className="flex justify-center py-10">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
                </div>
            ) : displayCoupons.length === 0 ? (
                <div className="text-center py-12">
                    <Gift size={32} className="mx-auto mb-4 text-stone-200" />
                    <p className="text-stone-400 text-sm">{t('couponView.empty')}</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {displayCoupons.map((coupon: UserCouponResponse) => {
                        const daysLeft = coupon.validUntil ? getDaysUntil(coupon.validUntil) : null;
                        const isExpired = daysLeft !== null && daysLeft <= 0 && coupon.status !== 'USED';
                        const isUsed = coupon.status === 'USED';

                        return (
                            <div
                                key={coupon.id}
                                className={`relative overflow-hidden rounded-2xl border p-5 transition-all ${isUsed || isExpired
                                        ? 'bg-stone-50 border-stone-100 opacity-60'
                                        : 'bg-white border-stone-100 hover:border-stone-300 hover:shadow-sm'
                                    }`}
                            >
                                <div className="flex items-start justify-between">
                                    <div className="flex-1">
                                        <div className="flex items-center space-x-2 mb-1">
                                            <Ticket size={16} className={isUsed ? 'text-stone-300' : 'text-purple-500'} />
                                            <span className="font-bold text-sm">{coupon.couponName}</span>
                                        </div>
                                        {coupon.couponDescription && (
                                            <p className="text-xs text-stone-400 ml-6 mb-2">{coupon.couponDescription}</p>
                                        )}
                                        <div className="ml-6 space-y-1">
                                            <p className="text-xs text-stone-400 flex items-center">
                                                <Tag size={12} className="mr-1" />
                                                {coupon.couponType === 'PERCENTAGE'
                                                    ? t('couponView.discount_percent', { value: coupon.discountValue })
                                                    : t('couponView.discount_fixed', {
                                                          value: `${coupon.discountValue?.toLocaleString() ?? ''}${t('common.currency_won')}`,
                                                      })}
                                                {coupon.maxDiscountAmount > 0 &&
                                                    ` ${t('couponView.max_discount', {
                                                        value: `${coupon.maxDiscountAmount.toLocaleString()}${t('common.currency_won')}`,
                                                    })}`}
                                            </p>
                                            {coupon.minOrderAmount > 0 && (
                                                <p className="text-xs text-stone-300">
                                                    {t('couponView.min_order', {
                                                        value: `${coupon.minOrderAmount.toLocaleString()}${t('common.currency_won')}`,
                                                    })}
                                                </p>
                                            )}
                                            <p className="text-xs text-stone-300 flex items-center">
                                                <Clock size={12} className="mr-1" />
                                                {t('couponView.valid_until', { date: coupon.validUntil?.split('T')[0] ?? '' })}
                                            </p>
                                        </div>
                                    </div>

                                    <div className="text-right">
                                        {isUsed ? (
                                            <span className="inline-flex items-center text-xs text-stone-400 bg-stone-100 px-3 py-1 rounded-full">
                                                <CheckCircle size={12} className="mr-1" />
                                                {t('couponView.used')}
                                            </span>
                                        ) : isExpired ? (
                                            <span className="inline-flex items-center text-xs text-red-400 bg-red-50 px-3 py-1 rounded-full">
                                                <XCircle size={12} className="mr-1" />
                                                {t('couponView.expired')}
                                            </span>
                                        ) : daysLeft !== null && daysLeft <= 7 ? (
                                            <span className="text-xs font-bold text-red-500">D-{daysLeft}</span>
                                        ) : null}
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
}
