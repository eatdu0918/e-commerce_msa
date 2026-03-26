import { useState, useEffect } from 'react';
import * as PortOne from '@portone/browser-sdk/v2';
import { v4 as uuidv4 } from 'uuid';
import { createOrder } from '../../api/services/order';
import { createPayment } from '../../api/services/payment';
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
    const [paymentMethod, setPaymentMethod] = useState('CREDIT_CARD');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    
    // Address state
    const [isAddressModalOpen, setIsAddressModalOpen] = useState(false);
    const [zonecode, setZonecode] = useState('');
    const [roadAddress, setRoadAddress] = useState('');
    const [detailAddress, setDetailAddress] = useState('');

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

    if (!isOpen) return null;

    const totalAmount = product.price * quantity;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!address.trim()) {
            setError('배송지 주소를 입력해주세요.');
            return;
        }
        
        setLoading(true);
        setError('');

        const paymentId = `ORD-${Date.now()}-${uuidv4().slice(0, 8)}`;

        let portonePayMethod: any = "CARD";
        let easyPayProvider: any = undefined;

        switch (paymentMethod) {
            case 'CREDIT_CARD':
                portonePayMethod = 'CARD';
                break;
            case 'BANK_TRANSFER':
                portonePayMethod = 'TRANSFER';
                break;
            case 'KAKAOPAY':
                portonePayMethod = 'EASY_PAY';
                easyPayProvider = 'KAKAOPAY';
                break;
            case 'TOSSPAY':
                portonePayMethod = 'EASY_PAY';
                easyPayProvider = 'TOSSPAY';
                break;
        }

        const paymentRequest: any = {
            storeId: "store-2dd12310-9af7-47c4-8a3c-372f87d36508", // User provided store ID
            channelKey: "channel-key-105a265f-d034-4b42-a586-76fb87979796",
            paymentId: paymentId,
            orderName: product.name,
            totalAmount: totalAmount,
            currency: "CURRENCY_KRW",
            payMethod: portonePayMethod,
            customer: {
                fullName: recipientName,
                phoneNumber: recipientPhone,
                address: {
                    addressLine1: address
                }
            },
        };

        if (easyPayProvider) {
            paymentRequest.easyPay = { easyPayProvider };
        }

        try {
            const response = await PortOne.requestPayment(paymentRequest);

            if (response.code != null) {
                setError(response.message || '결제에 실패했습니다.');
                setLoading(false);
                return;
            }

            const newOrder = await createOrder({
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
            });

            await createPayment({
                orderId: newOrder.id,
                paymentMethod,
            });

            onOrderSuccess(newOrder.id);
            onClose();
        } catch (_err: any) {
            setError('주문 및 결제 처리에 실패했습니다.');
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
                className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl relative cursor-default"
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
                        <p className="text-xs text-stone-500">{quantity}개 / ${totalAmount.toLocaleString()}</p>
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
                            placeholder="010-0000-0000"
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

                    <div>
                        <label className="block text-sm font-bold text-stone-700 mb-2">결제 수단</label>
                        <div className="grid grid-cols-2 gap-2 mb-2">
                            {[
                                { value: 'CREDIT_CARD', label: '신용카드' },
                                { value: 'BANK_TRANSFER', label: '계좌이체' },
                                { value: 'KAKAOPAY', label: '카카오페이' },
                                { value: 'TOSSPAY', label: '토스페이' },
                            ].map((method) => (
                                <button
                                    key={method.value}
                                    type="button"
                                    onClick={() => setPaymentMethod(method.value)}
                                    className={`p-3 rounded-xl border text-xs font-bold transition-all ${paymentMethod === method.value ? 'border-black bg-stone-50' : 'border-stone-100 hover:border-stone-200'}`}
                                >
                                    {method.label}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="pt-4 border-t border-stone-100 flex justify-between items-center font-bold">
                        <span>총 결제금액</span>
                        <span className="text-xl">${totalAmount.toLocaleString()}</span>
                    </div>

                    {error && <p className="text-red-500 text-sm">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-stone-800 transition-colors disabled:opacity-50"
                    >
                        {loading ? 'Processing...' : '결제하기'}
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
