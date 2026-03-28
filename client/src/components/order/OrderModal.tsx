import { useState, useEffect, useRef } from 'react';
import { getTossPayments } from '../../lib/tossPayments';
import { digitsOnlyPhone, isValidKoreanMobile } from '../../lib/koreanPhone';
import { v4 as uuidv4 } from 'uuid';
import type { UserResponse } from '../../api/services/user';
import type { Product } from '../../types/product';
import { X } from 'lucide-react';
import { useScrollLock } from '../../hooks/useScrollLock';
import AddressSearchModal from '../common/AddressSearchModal';

interface OrderModalProps {
    isOpen: boolean;
    onClose: () => void;
    product: Product;
    quantity: number;
    user: UserResponse;
    onOrderSuccess: (orderId: number) => void;
}

export default function OrderModal({ isOpen, onClose, product, quantity, user, onOrderSuccess }: OrderModalProps) {
    const [address, setAddress] = useState('');
    const [recipientName, setRecipientName] = useState(user.name || '');
    const [recipientPhone, setRecipientPhone] = useState(user.phoneNumber || '');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [widgetsReady, setWidgetsReady] = useState(false);

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
                    setError('결제 위젯을 불러오는 데 실패했습니다.');
                }
            }
        };

        initWidgets();

        return () => {
            cancelled = true;
            setWidgetsReady(false);
            widgetsRef.current = null;
        };
    }, [isOpen, totalAmount]);

    if (!isOpen) return null;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!address.trim()) {
            setError('배송지 주소를 입력해주세요.');
            return;
        }

        if (!recipientName.trim() || !recipientPhone.trim()) {
            setError('수령인과 연락처를 입력해주세요.');
            return;
        }

        if (!isValidKoreanMobile(recipientPhone)) {
            setError('연락처는 01012345678 또는 010-1234-5678 형식으로 입력해주세요.');
            return;
        }

        if (!widgetsRef.current) {
            setError('결제 위젯이 아직 준비되지 않았습니다.');
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
            setError('결제 시스템 연동 중 오류가 발생했습니다.');
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

                <h2 className="text-2xl font-bold mb-6">주문서 작성</h2>

                <div className="flex items-center space-x-4 mb-6 bg-stone-50 p-4 rounded-2xl">
                    <img src={product.image} alt={product.name} className="w-16 h-16 rounded-xl object-cover" />
                    <div className="text-left">
                        <h4 className="font-bold text-sm">{product.name}</h4>
                        <p className="text-xs text-stone-500">{quantity}개 / {totalAmount.toLocaleString()}원</p>
                    </div>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">수령인 이름</label>
                        <input
                            type="text"
                            value={recipientName}
                            onChange={(e) => setRecipientName(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="홍길동"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">수령인 전화번호</label>
                        <input
                            type="tel"
                            value={recipientPhone}
                            onChange={(e) => setRecipientPhone(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="01012345678 또는 010-0000-0000"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">배송지 주소</label>
                        <div className="flex space-x-2 mb-2">
                            <input
                                type="text"
                                value={zonecode}
                                readOnly
                                placeholder="우편번호"
                                className="w-24 px-4 py-3 bg-stone-100 border border-stone-200 rounded-xl text-sm focus:outline-none text-stone-500"
                            />
                            <button
                                type="button"
                                onClick={() => setIsAddressModalOpen(true)}
                                className="px-4 py-3 bg-black text-white rounded-xl text-sm font-bold hover:bg-stone-800 transition-all whitespace-nowrap"
                            >
                                주소 검색
                            </button>
                        </div>
                        <input
                            type="text"
                            value={roadAddress}
                            readOnly
                            placeholder="기본 주소"
                            className="w-full px-4 py-3 bg-stone-100 border border-stone-200 rounded-xl text-sm focus:outline-none mb-2 text-stone-500 cursor-pointer"
                            onClick={() => setIsAddressModalOpen(true)}
                        />
                        <input
                            type="text"
                            value={detailAddress}
                            onChange={(e) => setDetailAddress(e.target.value)}
                            placeholder="상세 주소를 입력하세요 (예: 101동 1201호)"
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                        />
                    </div>

                    {/* TossPayments 결제위젯 */}
                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">결제 수단</label>
                        <div id="modal-payment-method-widget" className="w-full" />
                    </div>

                    {/* 약관 동의 위젯 */}
                    <div id="modal-agreement-widget" className="w-full" />

                    <div className="pt-4 border-t border-stone-100 flex justify-between items-center font-bold">
                        <span>총 결제금액</span>
                        <span className="text-xl">{totalAmount.toLocaleString()}원</span>
                    </div>

                    {error && <p className="text-red-500 text-sm">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading || !widgetsReady}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50"
                    >
                        {loading ? '처리 중...' : !widgetsReady ? '결제 위젯 로딩 중...' : '결제하기'}
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
