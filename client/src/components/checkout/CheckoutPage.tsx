import { useState, useEffect, useRef } from 'react';
import { getTossPayments } from '../../lib/tossPayments';
import { digitsOnlyPhone, isValidKoreanMobile } from '../../lib/koreanPhone';
import { parseCheckoutShippingAddress } from '../../lib/parseCheckoutShippingAddress';
import { v4 as uuidv4 } from 'uuid';
import { useQuery } from '@tanstack/react-query';
import { ChevronLeft, Tag, Truck, CreditCard, ShoppingBag, Check, Ticket } from 'lucide-react';
import { getCart } from '../../api/services/cart';
import { getMyOrders, type OrderItemRequest } from '../../api/services/order';
import { getMyProfile } from '../../api/services/user';
import { getAvailableCoupons, calculateDiscount } from '../../api/services/coupon';
import type { UserCouponResponse, DiscountCalculationResponse } from '../../api/services/coupon';
import AddressSearchModal from '../common/AddressSearchModal';

import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function CheckoutPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const onBack = () => navigate(-1);

    const [shippingAddress, setShippingAddress] = useState('');
    const [recipientName, setRecipientName] = useState('');
    const [recipientPhone, setRecipientPhone] = useState('');
    const [selectedCouponId, setSelectedCouponId] = useState<number | null>(null);
    const [discountInfo, setDiscountInfo] = useState<DiscountCalculationResponse | null>(null);
    const [step, setStep] = useState<'shipping' | 'payment'>('shipping');
    const [error, setError] = useState('');
    const [isProcessing, setIsProcessing] = useState(false);

    const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
    const [zonecode, setZonecode] = useState('');
    const [roadAddress, setRoadAddress] = useState('');
    const [detailAddress, setDetailAddress] = useState('');

    // 토스페이먼츠 위젯 관련
    const widgetsRef = useRef<any>(null);
    const [widgetsReady, setWidgetsReady] = useState(false);

    const { data: cart, isLoading: cartLoading } = useQuery({
        queryKey: ['cart'],
        queryFn: getCart,
    });

    const { data: coupons } = useQuery({
        queryKey: ['coupons', 'available'],
        queryFn: getAvailableCoupons,
    });

    const { data: ordersPrefillPage, isFetched: ordersPrefillFetched } = useQuery({
        queryKey: ['orders', 'checkout-prefill'],
        queryFn: () => getMyOrders(0, 1),
        staleTime: 60_000,
        retry: false,
    });

    const { data: profilePrefill, isFetched: profilePrefillFetched } = useQuery({
        queryKey: ['profile'],
        queryFn: getMyProfile,
        staleTime: 60_000,
        retry: false,
    });

    const shippingPrefillAppliedRef = useRef(false);

    useEffect(() => {
        if (shippingPrefillAppliedRef.current) return;
        if (!ordersPrefillFetched || !profilePrefillFetched) return;

        const lastOrder = ordersPrefillPage?.content?.[0];
        const hasLastShippingInfo =
            lastOrder &&
            (lastOrder.recipientName?.trim() || lastOrder.recipientPhone?.trim() || lastOrder.shippingAddress?.trim());

        if (hasLastShippingInfo && lastOrder) {
            if (lastOrder.recipientName?.trim()) setRecipientName(lastOrder.recipientName);
            if (lastOrder.recipientPhone?.trim()) setRecipientPhone(lastOrder.recipientPhone);
            if (lastOrder.shippingAddress?.trim()) {
                const parsed = parseCheckoutShippingAddress(lastOrder.shippingAddress);
                if (parsed) {
                    setZonecode(parsed.zonecode);
                    setRoadAddress(parsed.roadAddress);
                    setDetailAddress(parsed.detailAddress);
                }
            }
        } else if (profilePrefill) {
            if (profilePrefill.name?.trim()) setRecipientName(profilePrefill.name);
            if (profilePrefill.phoneNumber?.trim()) setRecipientPhone(profilePrefill.phoneNumber);
        }

        shippingPrefillAppliedRef.current = true;
    }, [ordersPrefillFetched, ordersPrefillPage, profilePrefillFetched, profilePrefill]);

    const items = cart?.items || [];
    const totalPrice = cart?.totalPrice || 0;

    // Calculate discount when coupon changes
    useEffect(() => {
        if (selectedCouponId && totalPrice > 0) {
            calculateDiscount({ userCouponId: selectedCouponId, totalAmount: totalPrice })
                .then(setDiscountInfo)
                .catch(() => setDiscountInfo(null));
        } else {
            setDiscountInfo(null);
        }
    }, [selectedCouponId, totalPrice]);

    // Update shippingAddress whenever parts change
    useEffect(() => {
        if (roadAddress) {
            setShippingAddress(`(${zonecode}) ${roadAddress} ${detailAddress}`);
        }
    }, [zonecode, roadAddress, detailAddress]);

    const handleAddressComplete = (address: string, zonecode: string) => {
        setRoadAddress(address);
        setZonecode(zonecode);
        setDetailAddress('');
    };

    const finalAmount = discountInfo ? discountInfo.finalAmount : totalPrice;
    const discountAmount = discountInfo ? discountInfo.discountAmount : 0;

    // 결제 단계에 진입하면 위젯 초기화
    useEffect(() => {
        if (step !== 'payment' || finalAmount <= 0) return;

        let cancelled = false;

        const initWidgets = async () => {
            try {
                const tossPayments = await getTossPayments();
                const widgets = tossPayments.widgets({
                    customerKey: uuidv4(),
                });

                await widgets.setAmount({
                    currency: 'KRW',
                    value: Math.round(finalAmount),
                });

                if (cancelled) return;

                await widgets.renderPaymentMethods({
                    selector: '#payment-method-widget',
                });

                await widgets.renderAgreement({
                    selector: '#agreement-widget',
                    variantKey: 'AGREEMENT',
                });

                widgetsRef.current = widgets;
                setWidgetsReady(true);
            } catch (err) {
                console.error('결제 위젯 초기화 실패:', err);
                if (!cancelled) {
                    setError(t('checkout.widget_load_failed'));
                }
            }
        };

        initWidgets();

        return () => {
            cancelled = true;
            setWidgetsReady(false);
            widgetsRef.current = null;
        };
    }, [step, finalAmount, t]);

    // 금액 변경 시 위젯 업데이트
    useEffect(() => {
        if (widgetsRef.current && finalAmount > 0) {
            widgetsRef.current.setAmount({
                currency: 'KRW',
                value: Math.round(finalAmount),
            }).catch(console.error);
        }
    }, [finalAmount]);

    // 단계(step)가 변경될 때 에러 메시지 초기화
    useEffect(() => {
        setError('');
    }, [step]);

    const validateShippingStep = (): boolean => {
        if (!shippingAddress.trim() || !recipientName.trim() || !recipientPhone.trim()) {
            setError(t('checkout.fill_all_shipping'));
            return false;
        }
        if (!isValidKoreanMobile(recipientPhone)) {
            setError(t('checkout.phone_invalid'));
            return false;
        }
        setError('');
        return true;
    };

    const handlePayment = async () => {
        if (!validateShippingStep()) {
            return;
        }
        if (!widgetsRef.current) {
            setError(t('checkout.widget_not_ready'));
            return;
        }
        setError('');
        setIsProcessing(true);

        try {
            // 주문 데이터를 localStorage에 저장 (결제 성공 후 복원)
            const orderItems: OrderItemRequest[] = items.map(item => ({
                productId: item.productId,
                productName: item.productName,
                imageUrl: item.imageUrl,
                unitPrice: item.price,
                quantity: item.quantity,
            }));

            const pendingOrderData = {
                items: orderItems,
                shippingAddress,
                recipientName,
                recipientPhone,
                ...(selectedCouponId ? { userCouponId: selectedCouponId } : {}),
                paymentMethod: 'TOSSPAYMENTS',
            };

            localStorage.setItem('pendingOrderData', JSON.stringify(pendingOrderData));

            const orderId = `ORD-${Date.now()}-${uuidv4().slice(0, 8)}`;

            // 결제위젯으로 결제 요청
            await widgetsRef.current.requestPayment({
                orderId: orderId,
                orderName:
                    items.length > 1
                        ? t('checkout.order_name_extra', { name: items[0].productName, count: items.length - 1 })
                        : items[0].productName,
                customerName: recipientName,
                customerMobilePhone: digitsOnlyPhone(recipientPhone),
                successUrl: `${window.location.origin}/payment/success`,
                failUrl: `${window.location.origin}/payment/fail`,
            });

        } catch (_error: any) {
            setError(t('checkout.payment_system_error'));
            setIsProcessing(false);
        }
    };

    if (cartLoading) {
        return (
            <div className="min-h-screen flex justify-center items-center">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-black"></div>
            </div>
        );
    }

    if (items.length === 0) {
        return (
            <div className="min-h-screen pt-12 pb-24 bg-[#f9f7f2]">
                <div className="max-w-3xl mx-auto px-6 text-center py-20">
                    <ShoppingBag size={48} className="mx-auto mb-6 text-stone-300" />
                    <h2 className="text-2xl font-bold mb-4">{t('checkout.cart_empty')}</h2>
                    <button onClick={onBack} className="bg-black text-white px-8 py-3 rounded-full text-sm font-bold">
                        {t('checkout.go_shopping')}
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen pt-12 pb-24 bg-[#f9f7f2]">
            <div className="max-w-4xl mx-auto px-6">
                <button onClick={onBack} className="flex items-center text-stone-400 hover:text-black transition-colors mb-8 group">
                    <ChevronLeft size={20} className="mr-2 transition-transform group-hover:-translate-x-1" />
                    <span className="text-sm font-medium">{t('checkout.back_short')}</span>
                </button>

                <h2 className="text-4xl font-bold tracking-tight mb-10 text-stone-900 text-left">{t('checkout.page_title')}</h2>

                {/* Steps */}
                <div className="flex items-center justify-center space-x-4 mb-12">
                    <button
                        onClick={() => setStep('shipping')}
                        className={`flex items-center space-x-2 px-5 py-2.5 rounded-full text-sm font-bold transition-all ${step === 'shipping' ? 'bg-black text-white' : 'bg-stone-100 text-stone-400'}`}
                    >
                        <Truck size={16} />
                        <span>{t('checkout.step_shipping')}</span>
                    </button>
                    <div className="w-8 h-px bg-stone-200" />
                    <button
                        onClick={() => {
                            setError('');
                            if (validateShippingStep()) setStep('payment');
                        }}
                        className={`flex items-center space-x-2 px-5 py-2.5 rounded-full text-sm font-bold transition-all ${step === 'payment' ? 'bg-black text-white' : 'bg-stone-100 text-stone-400'}`}
                    >
                        <CreditCard size={16} />
                        <span>{t('checkout.step_payment')}</span>
                    </button>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Left: Form */}
                    <div className="lg:col-span-2 space-y-6">
                        {step === 'shipping' && (
                            <div className="bg-white p-8 rounded-[30px] shadow-sm border border-stone-100">
                                <h3 className="text-lg font-bold mb-6 flex items-center">
                                    <Truck size={20} className="mr-3 text-blue-500" />
                                    {t('checkout.shipping_section')}
                                </h3>
                                <div className="space-y-5">
                                    <div>
                                        <label className="block text-xs font-bold text-stone-400 uppercase tracking-wider mb-2">{t('checkout.recipient')}</label>
                                        <input
                                            type="text"
                                            value={recipientName}
                                            onChange={(e) => setRecipientName(e.target.value)}
                                            placeholder={t('checkout.placeholder_name')}
                                            className="w-full px-5 py-3.5 bg-stone-50 border border-stone-100 rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-black/10 transition-all"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-bold text-stone-400 uppercase tracking-wider mb-2">{t('checkout.contact')}</label>
                                        <input
                                            type="tel"
                                            value={recipientPhone}
                                            onChange={(e) => setRecipientPhone(e.target.value)}
                                            placeholder={t('checkout.placeholder_phone')}
                                            className="w-full px-5 py-3.5 bg-stone-50 border border-stone-100 rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-black/10 transition-all"
                                        />
                                    </div>
                                    <div>
                                        <label className="block text-xs font-bold text-stone-400 uppercase tracking-wider mb-2">{t('checkout.shipping_address')}</label>
                                        <div className="flex space-x-2 mb-2">
                                            <input
                                                type="text"
                                                value={zonecode}
                                                readOnly
                                                placeholder={t('checkout.postcode_placeholder')}
                                                className="w-24 px-5 py-3.5 bg-stone-100 border border-stone-100 rounded-2xl text-sm focus:outline-none text-stone-500"
                                            />
                                            <button
                                                onClick={() => setIsAddressModalOpen(true)}
                                                className="px-4 py-3.5 bg-black text-white rounded-2xl text-sm font-bold hover:bg-stone-800 transition-all whitespace-nowrap"
                                            >
                                                {t('checkout.address_search')}
                                            </button>
                                        </div>
                                        <input
                                            type="text"
                                            value={roadAddress}
                                            readOnly
                                            placeholder={t('checkout.address_base_placeholder')}
                                            className="w-full px-5 py-3.5 bg-stone-100 border border-stone-100 rounded-2xl text-sm focus:outline-none mb-2 text-stone-500 cursor-pointer"
                                            onClick={() => setIsAddressModalOpen(true)}
                                        />
                                        <input
                                            type="text"
                                            value={detailAddress}
                                            onChange={(e) => setDetailAddress(e.target.value)}
                                            placeholder={t('checkout.address_detail_placeholder')}
                                            className="w-full px-5 py-3.5 bg-stone-50 border border-stone-100 rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-black/10 transition-all"
                                        />
                                    </div>
                                </div>
                                <button
                                    onClick={() => {
                                        if (validateShippingStep()) setStep('payment');
                                    }}
                                    className="w-full mt-8 bg-black text-white py-4 rounded-2xl text-sm font-bold hover:bg-stone-800 transition-all"
                                >
                                    {t('checkout.to_payment_step')}
                                </button>
                                <AddressSearchModal
                                    isOpen={isAddressModalOpen}
                                    onClose={() => setIsAddressModalOpen(false)}
                                    onComplete={handleAddressComplete}
                                />
                            </div>
                        )}

                        {step === 'payment' && (
                            <>
                                {/* Coupon */}
                                <div className="bg-white p-8 rounded-[30px] shadow-sm border border-stone-100">
                                    <h3 className="text-lg font-bold mb-6 flex items-center">
                                        <Ticket size={20} className="mr-3 text-purple-500" />
                                        {t('checkout.coupon_apply')}
                                    </h3>
                                    {coupons && coupons.length > 0 ? (
                                        <div className="space-y-3">
                                            <button
                                                onClick={() => setSelectedCouponId(null)}
                                                className={`w-full p-4 rounded-2xl border text-left text-sm transition-all ${!selectedCouponId ? 'border-black bg-stone-50 font-bold' : 'border-stone-100 hover:border-stone-300'}`}
                                            >
                                                <div className="flex items-center justify-between">
                                                    <span>{t('checkout.coupon_none_selected')}</span>
                                                    {!selectedCouponId && <Check size={16} className="text-green-500" />}
                                                </div>
                                            </button>
                                            {coupons.map((coupon: UserCouponResponse) => (
                                                <button
                                                    key={coupon.id}
                                                    onClick={() => setSelectedCouponId(coupon.id)}
                                                    className={`w-full p-4 rounded-2xl border text-left text-sm transition-all ${selectedCouponId === coupon.id ? 'border-black bg-stone-50' : 'border-stone-100 hover:border-stone-300'}`}
                                                >
                                                    <div className="flex items-center justify-between">
                                                        <div>
                                                            <p className="font-bold">{coupon.couponName}</p>
                                                            <p className="text-xs text-stone-400 mt-1">
                                                                {coupon.couponType === 'PERCENTAGE'
                                                                    ? t('checkout.discount_percent', { value: coupon.discountValue })
                                                                    : t('checkout.discount_fixed', {
                                                                          value: `${coupon.discountValue.toLocaleString()}${t('common.currency_won')}`,
                                                                      })}
                                                                {coupon.minOrderAmount > 0 &&
                                                                    ` · ${t('checkout.min_order', {
                                                                        value: `${coupon.minOrderAmount.toLocaleString()}${t('common.currency_won')}`,
                                                                    })}`}
                                                            </p>
                                                            <p className="text-[10px] text-stone-300 mt-1">
                                                                {t('checkout.coupon_valid_until', {
                                                                    date: coupon.validUntil?.split('T')[0] ?? '',
                                                                })}
                                                            </p>
                                                        </div>
                                                        {selectedCouponId === coupon.id && <Check size={16} className="text-green-500" />}
                                                    </div>
                                                </button>
                                            ))}
                                        </div>
                                    ) : (
                                        <p className="text-sm text-stone-400 text-center py-4">{t('checkout.no_coupons')}</p>
                                    )}
                                </div>

                                {/* TossPayments 결제위젯 */}
                                <div className="bg-white p-8 rounded-[30px] shadow-sm border border-stone-100">
                                    <h3 className="text-lg font-bold mb-6 flex items-center">
                                        <CreditCard size={20} className="mr-3 text-blue-500" />
                                        {t('checkout.payment_method')}
                                    </h3>
                                    <div id="payment-method-widget" className="w-full" />
                                </div>

                                {/* 약관 동의 위젯 */}
                                <div className="bg-white p-8 rounded-[30px] shadow-sm border border-stone-100">
                                    <div id="agreement-widget" className="w-full" />
                                </div>
                            </>
                        )}
                    </div>

                    {/* Right: Order Summary */}
                    <div className="lg:col-span-1">
                        <div className="bg-white p-8 rounded-[30px] shadow-sm border border-stone-100 sticky top-24">
                            <h3 className="text-lg font-bold mb-6 flex items-center">
                                <Tag size={20} className="mr-3 text-green-500" />
                                {t('checkout.order_summary')}
                            </h3>

                            <div className="space-y-4 mb-6 max-h-60 overflow-y-auto no-scrollbar">
                                {items.map((item) => (
                                    <div key={item.cartItemId} className="flex space-x-4">
                                        <div className="w-14 h-14 bg-stone-100 rounded-xl overflow-hidden flex-shrink-0">
                                            {item.imageUrl ? (
                                                <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover" />
                                            ) : (
                                                <div className="w-full h-full flex items-center justify-center text-stone-300">
                                                    <ShoppingBag size={16} />
                                                </div>
                                            )}
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <p className="text-sm font-medium truncate">{item.productName}</p>
                                            <p className="text-xs text-stone-400">{t('checkout.qty', { count: item.quantity })}</p>
                                        </div>
                                        <p className="text-sm font-bold whitespace-nowrap">
                                            {(item.price * item.quantity).toLocaleString()}
                                            {t('common.currency_won')}
                                        </p>
                                    </div>
                                ))}
                            </div>

                            <div className="border-t border-stone-100 pt-4 space-y-3 text-sm">
                                <div className="flex justify-between text-stone-500">
                                    <span>{t('checkout.product_amount')}</span>
                                    <span>
                                        {totalPrice.toLocaleString()}
                                        {t('common.currency_won')}
                                    </span>
                                </div>
                                {discountAmount > 0 && (
                                    <div className="flex justify-between text-red-500">
                                        <span>{t('checkout.coupon_discount')}</span>
                                        <span>
                                            -{discountAmount.toLocaleString()}
                                            {t('common.currency_won')}
                                        </span>
                                    </div>
                                )}
                                <div className="flex justify-between text-stone-500">
                                    <span>{t('checkout.shipping_fee')}</span>
                                    <span className="text-green-600 font-medium">{t('checkout.shipping_free')}</span>
                                </div>
                            </div>

                            <div className="border-t border-stone-100 pt-4 mt-4 flex justify-between items-center">
                                <span className="font-bold">{t('checkout.final_amount')}</span>
                                <span className="text-2xl font-bold">
                                    {finalAmount.toLocaleString()}
                                    {t('common.currency_won')}
                                </span>
                            </div>

                            {error && (
                                <p className="text-red-500 text-xs mt-4 bg-red-50 p-3 rounded-xl">{error}</p>
                            )}

                            {step === 'payment' && (
                                <button
                                    onClick={handlePayment}
                                    disabled={isProcessing || !widgetsReady}
                                    className="w-full mt-6 bg-black text-white py-4 rounded-2xl text-sm font-bold hover:bg-stone-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed active:scale-[0.98]"
                                >
                                    {isProcessing ? (
                                        <span className="flex items-center justify-center space-x-2">
                                            <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                                            <span>{t('auth.processing')}</span>
                                        </span>
                                    ) : !widgetsReady ? (
                                        <span className="flex items-center justify-center space-x-2">
                                            <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                                            <span>{t('checkout.widget_loading')}</span>
                                        </span>
                                    ) : (
                                        t('checkout.pay_with_amount', {
                                            amount: `${finalAmount.toLocaleString()}${t('common.currency_won')}`,
                                        })
                                    )}
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
