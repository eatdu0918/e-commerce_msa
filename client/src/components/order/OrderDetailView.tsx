import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { CreditCard, MapPin, Phone, User, ShoppingBag, XCircle, CheckCircle2 } from 'lucide-react';
import { getOrderDetail, cancelOrder } from '../../api/services/order';
import type { OrderDetailResponse } from '../../api/services/order';
import { useState } from 'react';

interface OrderDetailViewProps {
    orderId: number;
    onBack: () => void;
}

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPING', 'DELIVERED'];
const STATUS_LABELS: Record<string, string> = {
    PENDING: '주문 접수',
    CONFIRMED: '주문 확인',
    PREPARING: '상품 준비',
    SHIPPING: '배송 중',
    DELIVERED: '배송 완료',
    CANCELLED: '주문 취소',
};

const CANCELLABLE_STATUSES = ['PENDING', 'CONFIRMED', 'PREPARING'];

export default function OrderDetailView({ orderId, onBack }: OrderDetailViewProps) {
    const queryClient = useQueryClient();
    const [showCancelConfirm, setShowCancelConfirm] = useState(false);

    const { data: order, isLoading } = useQuery<OrderDetailResponse>({
        queryKey: ['order', orderId],
        queryFn: () => getOrderDetail(orderId),
    });

    const cancelMutation = useMutation({
        mutationFn: () => cancelOrder(orderId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['order', orderId] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
            setShowCancelConfirm(false);
        },
    });

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-20">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-black"></div>
            </div>
        );
    }

    if (!order) {
        return (
            <div className="text-center py-20 text-stone-400">
                <p>주문 정보를 찾을 수 없습니다.</p>
                <button onClick={onBack} className="mt-4 text-black underline font-bold text-sm">돌아가기</button>
            </div>
        );
    }

    const isCancelled = order.status === 'CANCELLED';
    const canCancel = CANCELLABLE_STATUSES.includes(order.status);
    const currentStepIndex = STATUS_STEPS.indexOf(order.status);

    return (
        <div className="space-y-8">
            {/* Order Header */}
            <div className="flex flex-col md:flex-row md:items-center md:justify-between">
                <div>
                    <p className="text-xs font-bold text-stone-400 uppercase tracking-widest mb-1">주문번호 {order.orderNumber}</p>
                    <p className="text-xs text-stone-300">{order.createdAt?.split('T')[0]} 주문</p>
                </div>
                <span className={`inline-block px-4 py-1.5 rounded-full text-xs font-bold mt-3 md:mt-0 w-fit ${isCancelled ? 'bg-red-50 text-red-500' : order.status === 'DELIVERED' ? 'bg-green-50 text-green-600' : 'bg-blue-50 text-blue-600'
                    }`}>
                    {STATUS_LABELS[order.status] || order.statusDescription}
                </span>
            </div>

            {/* Status Tracker */}
            {!isCancelled && (
                <div className="bg-stone-50 rounded-3xl p-6 border border-stone-100">
                    <div className="flex items-center justify-between relative">
                        {/* Progress line */}
                        <div className="absolute top-4 left-0 right-0 h-0.5 bg-stone-200 mx-8" />
                        <div
                            className="absolute top-4 left-0 h-0.5 bg-black mx-8 transition-all duration-700"
                            style={{ width: `${Math.max(0, (currentStepIndex / (STATUS_STEPS.length - 1)) * 100 - 8)}%` }}
                        />

                        {STATUS_STEPS.map((status, idx) => (
                            <div key={status} className="relative z-10 flex flex-col items-center">
                                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all ${idx <= currentStepIndex ? 'bg-black text-white' : 'bg-white border-2 border-stone-200 text-stone-300'
                                    }`}>
                                    {idx < currentStepIndex ? <CheckCircle2 size={16} /> : idx + 1}
                                </div>
                                <span className={`text-[10px] mt-2 font-medium whitespace-nowrap ${idx <= currentStepIndex ? 'text-black' : 'text-stone-300'
                                    }`}>
                                    {STATUS_LABELS[status]}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {isCancelled && (
                <div className="bg-red-50 rounded-3xl p-6 border border-red-100 flex items-center space-x-4">
                    <XCircle size={24} className="text-red-400" />
                    <div>
                        <p className="font-bold text-red-600">주문이 취소되었습니다</p>
                        <p className="text-xs text-red-400 mt-1">환불은 결제 수단에 따라 1~3영업일 소요됩니다.</p>
                    </div>
                </div>
            )}

            {/* Items */}
            <div>
                <h4 className="text-xs font-bold text-stone-400 uppercase tracking-widest mb-4">주문 상품</h4>
                <div className="space-y-4">
                    {order.items?.map((item) => (
                        <div key={item.id} className="flex space-x-5 bg-stone-50 p-4 rounded-2xl border border-stone-100">
                            <div className="w-20 h-20 bg-white rounded-xl overflow-hidden flex-shrink-0">
                                {item.imageUrl ? (
                                    <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover" />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center text-stone-300">
                                        <ShoppingBag size={20} />
                                    </div>
                                )}
                            </div>
                            <div className="flex-1">
                                <h5 className="font-bold text-sm">{item.productName}</h5>
                                <p className="text-xs text-stone-400 mt-1">수량 {item.quantity}개</p>
                                <p className="font-bold text-sm mt-2">{item.totalPrice?.toLocaleString()}원</p>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Shipping / Payment Info */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-stone-50 p-6 rounded-2xl border border-stone-100">
                    <h4 className="text-xs font-bold text-stone-400 uppercase tracking-widest mb-4 flex items-center">
                        <MapPin size={14} className="mr-2" /> 배송 정보
                    </h4>
                    <div className="space-y-2 text-sm">
                        <div className="flex items-center space-x-2">
                            <User size={14} className="text-stone-300" />
                            <span>{order.recipientName}</span>
                        </div>
                        <div className="flex items-center space-x-2">
                            <Phone size={14} className="text-stone-300" />
                            <span>{order.recipientPhone}</span>
                        </div>
                        <div className="flex items-start space-x-2">
                            <MapPin size={14} className="text-stone-300 mt-0.5" />
                            <span>{order.shippingAddress}</span>
                        </div>
                    </div>
                </div>

                <div className="bg-stone-50 p-6 rounded-2xl border border-stone-100">
                    <h4 className="text-xs font-bold text-stone-400 uppercase tracking-widest mb-4 flex items-center">
                        <CreditCard size={14} className="mr-2" /> 결제 정보
                    </h4>
                    <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                            <span className="text-stone-400">상품 금액</span>
                            <span>{order.totalAmount?.toLocaleString()}원</span>
                        </div>
                        {order.discountAmount > 0 && (
                            <div className="flex justify-between text-red-500">
                                <span>할인</span>
                                <span>-{order.discountAmount.toLocaleString()}원</span>
                            </div>
                        )}
                        <div className="flex justify-between font-bold pt-2 border-t border-stone-200">
                            <span>결제 금액</span>
                            <span>{order.finalAmount?.toLocaleString()}원</span>
                        </div>
                        {order.payment && (
                            <div className="flex justify-between text-xs text-stone-400 pt-2">
                                <span>{order.payment.paymentMethod}</span>
                                <span>{order.payment.status}</span>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {/* Cancel Button */}
            {canCancel && !showCancelConfirm && (
                <button
                    onClick={() => setShowCancelConfirm(true)}
                    className="w-full py-4 bg-white border border-red-200 text-red-500 rounded-2xl text-sm font-bold hover:bg-red-50 transition-all"
                >
                    주문 취소하기
                </button>
            )}

            {showCancelConfirm && (
                <div className="bg-red-50 p-6 rounded-2xl border border-red-200">
                    <p className="font-bold text-red-600 mb-2">정말 주문을 취소하시겠습니까?</p>
                    <p className="text-xs text-red-400 mb-6">취소 후에는 되돌릴 수 없습니다.</p>
                    <div className="flex space-x-3">
                        <button
                            onClick={() => cancelMutation.mutate()}
                            disabled={cancelMutation.isPending}
                            className="flex-1 py-3 bg-red-500 text-white rounded-xl text-sm font-bold hover:bg-red-600 transition-colors disabled:opacity-50"
                        >
                            {cancelMutation.isPending ? '처리 중...' : '주문 취소'}
                        </button>
                        <button
                            onClick={() => setShowCancelConfirm(false)}
                            className="flex-1 py-3 bg-white border border-stone-200 rounded-xl text-sm font-bold hover:bg-stone-50 transition-colors"
                        >
                            돌아가기
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}
