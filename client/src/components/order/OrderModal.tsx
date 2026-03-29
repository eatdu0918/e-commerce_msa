import { useState, useEffect, useRef } from 'react';
import { getTossPayments } from '../../lib/tossPayments';
import { digitsOnlyPhone, isValidKoreanMobile } from '../../lib/koreanPhone';
import { v4 as uuidv4 } from 'uuid';
import type { UserResponse } from '../../api/services/user';
import type { Product } from '../../types/product';
import { X } from 'lucide-react';
import { useScrollLock } from '../../hooks/useScrollLock';
import AddressSearchModal from '../common/AddressSearchModal';
import { useTranslation } from 'react-i18next';

interface OrderModalProps {
    isOpen: boolean;
    onClose: () => void;
    product: Product;
    quantity: number;
    user: UserResponse;
    onOrderSuccess: (orderId: number) => void;
}

export default function OrderModal({ isOpen, onClose, product, quantity, user, onOrderSuccess }: OrderModalProps) {
    const { t } = useTranslation();
    const [address, setAddress] = useState('');
    const [recipientName, setRecipientName] = useState(user.name || '');
    const [recipientPhone, setRecipientPhone] = useState(user.phoneNumber || '');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [widgetsReady, setWidgetsReady] = useState(false);
    const [skipConfirmAndPreparing, setSkipConfirmAndPreparing] = useState(false);
    const [skipShippingAndDelivered, setSkipShippingAndDelivered] = useState(false);

    // Address state
    const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
    const [zonecode, setZonecode] = useState('');
    const [roadAddress, setRoadAddress] = useState('');
    const [detailAddress, setDetailAddress] = useState('');

    // 토스페이먼츠 위젯 관련
    const widgetsRef = useRef<any>(null);

    useEffect(() => {
        if (roadAddress) {
            setAddress(`(${zonecode}) ${roadAddress} ${detailAddress}`);
        }
    }, [zonecode, roadAddress, detailAddress]);

    const handleAddressComplete = (addressStr: string, zonecodeStr: string) => {
        setRoadAddress(addressStr);
        setZonecode(zonecodeStr);
        setDetailAddress('');
    };

    useScrollLock(isOpen);

    // suppress unused warning
    void onOrderSuccess;

    const totalAmount = product.price * quantity;

    // 모달 열리면 위젯 초기화
    useEffect(() => {
        if (!isOpen || totalAmount <= 0) return;

        let cancelled = false;

        const initWidgets = async () => {
            // DOM이 렌더링될 때까지 약간 대기
            await new Promise(resolve => setTimeout(resolve, 100));

            try {
                const tossPayments = await getTossPayments();
                const widgets = tossPayments.widgets({
                    customerKey: uuidv4(),
                });

                await widgets.setAmount({
                    currency: 'KRW',
                    value: Math.round(totalAmount),
                });

                if (cancelled) return;

                await widgets.renderPaymentMethods({
                    selector: '#modal-payment-method-widget',
                });

                await widgets.renderAgreement({
                    selector: '#modal-agreement-widget',
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
    }, [isOpen, totalAmount, t]);

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!address.trim()) {
            setError(t('checkout.fill_address'));
            return;
        }

        if (!recipientName.trim() || !recipientPhone.trim()) {
            setError(t('checkout.fill_recipient'));
            return;
        }

        if (!isValidKoreanMobile(recipientPhone)) {
            setError(t('checkout.phone_invalid'));
            return;
        }

        if (!widgetsRef.current) {
            setError(t('checkout.widget_not_ready'));
            return;
        }

        setLoading(true);
        setError('');

        try {
            // 주문 데이터를 localStorage에 저장 (결제 성공 후 복원)
            const pendingOrderData = {
                items: [{
                    productId: product.id,
                    productName: product.name,
                    imageUrl: product.image,
                    unitPrice: product.price,
                    quantity: quantity,
                }],
                shippingAddress: address,
                recipientName,
                recipientPhone,
                paymentMethod: 'TOSSPAYMENTS',
                skipConfirmAndPreparing,
                skipShippingAndDelivered,
            };

            localStorage.setItem('pendingOrderData', JSON.stringify(pendingOrderData));

            const orderId = `ORD-${Date.now()}-${uuidv4().slice(0, 8)}`;

            // 결제위젯으로 결제 요청
            await widgetsRef.current.requestPayment({
                orderId: orderId,
                orderName: product.name,
                customerName: recipientName,
                customerMobilePhone: digitsOnlyPhone(recipientPhone),
                successUrl: `${window.location.origin}/payment/success`,
                failUrl: `${window.location.origin}/payment/fail`,
            });

        } catch (_err: any) {
            setError(t('checkout.payment_system_error'));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm cursor-pointer"
            onClick={onClose}
        >
            <div
                className="bg-white rounded-3xl p-8 w-full max-w-lg max-h-[90vh] overflow-y-auto shadow-2xl relative cursor-default"
                onClick={(e) => e.stopPropagation()}
            >
                <button
                    onClick={onClose}
                    className="absolute top-6 right-6 text-stone-400 hover:text-black transition-colors"
                >
                    <X size={24} />
                </button>

                <h2 className="text-2xl font-bold mb-6">{t('checkout.order_form_title')}</h2>

                <div className="flex items-center space-x-4 mb-6 bg-stone-50 p-4 rounded-2xl">
                    <img src={product.image} alt={product.name} className="w-16 h-16 rounded-xl object-cover" />
                    <div className="text-left">
                        <h4 className="font-bold text-sm">{product.name}</h4>
                        <p className="text-xs text-stone-500">
                            {t('checkout.quantity_total', {
                                quantity,
                                amount: `${totalAmount.toLocaleString()}${t('common.currency_won')}`,
                            })}
                        </p>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('checkout.recipient_name')}</label>
                        <input
                            type="text"
                            value={recipientName}
                            onChange={(e) => setRecipientName(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder={t('checkout.placeholder_name')}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('checkout.recipient_phone')}</label>
                        <input
                            type="tel"
                            value={recipientPhone}
                            onChange={(e) => setRecipientPhone(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder={t('checkout.placeholder_phone')}
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('checkout.shipping_address')}</label>
                        <div className="flex space-x-2 mb-2">
                            <input
                                type="text"
                                value={zonecode}
                                readOnly
                                placeholder={t('checkout.postcode_placeholder')}
                                className="w-24 px-4 py-3 bg-stone-100 border border-stone-200 rounded-xl text-sm focus:outline-none text-stone-500"
                            />
                            <button
                                type="button"
                                onClick={() => setIsAddressModalOpen(true)}
                                className="px-4 py-3 bg-black text-white rounded-xl text-sm font-bold hover:bg-stone-800 transition-all whitespace-nowrap"
                            >
                                {t('checkout.address_search')}
                            </button>
                        </div>
                        <input
                            type="text"
                            value={roadAddress}
                            readOnly
                            placeholder={t('checkout.address_base_placeholder')}
                            className="w-full px-4 py-3 bg-stone-100 border border-stone-200 rounded-xl text-sm focus:outline-none mb-2 text-stone-500 cursor-pointer"
                            onClick={() => setIsAddressModalOpen(true)}
                        />
                        <input
                            type="text"
                            value={detailAddress}
                            onChange={(e) => setDetailAddress(e.target.value)}
                            placeholder={t('checkout.address_detail_placeholder')}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                        />
                    </div>

                    {/* TossPayments 결제위젯 */}
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('checkout.payment_method')}</label>
                        <div id="modal-payment-method-widget" className="w-full" />
                    </div>

                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">{t('checkout.fulfillment_skip_title')}</label>
                        <label className="flex items-start gap-3 cursor-pointer group">
                            <input
                                type="checkbox"
                                className="mt-1 rounded border-stone-300 text-black focus:ring-black"
                                checked={skipConfirmAndPreparing}
                                onChange={(e) => {
                                    const v = e.target.checked;
                                    setSkipConfirmAndPreparing(v);
                                    if (!v) setSkipShippingAndDelivered(false);
                                }}
                            />
                            <span className="text-sm">
                                <span className="font-semibold text-stone-800">{t('checkout.fulfillment_skip_confirm_preparing')}</span>
                                <span className="text-xs text-stone-500 block mt-0.5">
                                    {t('checkout.fulfillment_skip_confirm_preparing_hint')}
                                </span>
                            </span>
                        </label>
                        <label
                            className={`flex items-start gap-3 mt-3 ${skipConfirmAndPreparing ? 'cursor-pointer' : 'cursor-not-allowed opacity-50'}`}
                        >
                            <input
                                type="checkbox"
                                className="mt-1 rounded border-stone-300 text-black focus:ring-black disabled:opacity-60"
                                disabled={!skipConfirmAndPreparing}
                                checked={skipShippingAndDelivered}
                                onChange={(e) => setSkipShippingAndDelivered(e.target.checked)}
                            />
                            <span className="text-sm">
                                <span className="font-semibold text-stone-800">{t('checkout.fulfillment_skip_shipping_delivered')}</span>
                                <span className="text-xs text-stone-500 block mt-0.5">
                                    {t('checkout.fulfillment_skip_shipping_delivered_hint')}
                                </span>
                                {!skipConfirmAndPreparing && (
                                    <span className="text-xs text-amber-700 block mt-1">
                                        {t('checkout.fulfillment_skip_shipping_requires_first')}
                                    </span>
                                )}
                            </span>
                        </label>
                    </div>

                    {/* 약관 동의 위젯 */}
                    <div id="modal-agreement-widget" className="w-full" />

                    <div className="pt-4 border-t border-stone-100 flex justify-between items-center font-bold">
                        <span>{t('checkout.total_payment')}</span>
                        <span className="text-xl">{totalAmount.toLocaleString()}{t('common.currency_won')}</span>
                    </div>

                    {error && <p className="text-red-500 text-sm">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading || !widgetsReady}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50"
                    >
                        {loading ? t('auth.processing') : !widgetsReady ? t('checkout.widget_loading') : t('checkout.pay_cta')}
                    </button>
                </form>
                <AddressSearchModal
                    isOpen={isAddressModalOpen}
                    onClose={() => setIsAddressModalOpen(false)}
                    onComplete={handleAddressComplete}
                />
            </div>
        </div>
    );
}
