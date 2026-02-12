import { useState } from 'react';
import { createOrder } from '../../api/services/order';
import type { UserResponse } from '../../api/services/user';
import type { Product } from '../../types/product';
import { X } from 'lucide-react';

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

    if (!isOpen) return null;

    const totalAmount = product.price * quantity;

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const newOrder = await createOrder({
                items: [{
                    productId: product.id,
                    productName: product.name,
                    unitPrice: product.price,
                    quantity: quantity,
                }],
                shippingAddress: address,
                recipientName,
                recipientPhone,
            });
            alert('주문이 완료되었습니다!');
            onOrderSuccess(newOrder.id);
            onClose();
        } catch (_err: any) {
            setError('주문 생성에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-[200] flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-3xl p-8 w-full max-w-md shadow-2xl relative">
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
                        <p className="text-xs text-stone-500">{quantity}개 / ₩{totalAmount.toLocaleString()}</p>
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
                        <input
                            type="text"
                            value={address}
                            onChange={(e) => setAddress(e.target.value)}
                            className="w-full px-4 py-3 rounded-xl border border-stone-200 focus:outline-none focus:ring-2 focus:ring-black transition-all"
                            placeholder="서울특별시 강남구..."
                            required
                        />
                    </div>

                    <div className="pt-4 border-t border-stone-100 flex justify-between items-center font-bold">
                        <span>총 결제금액</span>
                        <span className="text-xl">₩{totalAmount.toLocaleString()}</span>
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
            </div>
        </div>
    );
}
