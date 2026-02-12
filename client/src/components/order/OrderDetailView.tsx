import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { formatDateTime } from '../../utils/date';
import { CreditCard, MapPin, Package, Clock, ArrowLeft } from 'lucide-react';
import { getOrderDetail, cancelOrder } from '../../api/services/order';
import type { OrderDetailResponse } from '../../api/services/order';
import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';

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

export default function OrderDetailView() {
    const { id } = useParams();
    const orderId = Number(id);
    const navigate = useNavigate();
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
                <button onClick={() => navigate('/me/orders')} className="mt-4 text-black underline font-bold text-sm">돌아가기</button>
            </div>
        );
    }

    const isCancelled = order.status === 'CANCELLED';
    const canCancel = CANCELLABLE_STATUSES.includes(order.status);
    const currentStepIndex = STATUS_STEPS.indexOf(order.status);

    return (
        <div className="max-w-3xl mx-auto px-6 py-12">
            <button
                onClick={() => navigate('/me/orders')}
                className="flex items-center text-stone-500 hover:text-black transition-colors mb-8"
            >
                <ArrowLeft size={20} className="mr-2" />
                <span className="font-medium">목록으로 돌아가기</span>
            </button>

            <div className="bg-white rounded-3xl shadow-sm border border-stone-100 overflow-hidden">
                <div className="p-8 border-b border-stone-100 bg-stone-50/50 flex justify-between items-center">
                    <div>
                        <div className="flex items-center space-x-3 mb-2">
                            <span className="font-bold text-lg">주문 #{order.orderNumber}</span>
                            <span className={`px-3 py-1 rounded-full text-xs font-bold ${order.status === 'DELIVERED' ? 'bg-green-100 text-green-700' :
                                order.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
                                    'bg-blue-100 text-blue-700'
                                }`}>
                                {STATUS_LABELS[order.status] || order.statusDescription}
                            </span>
                        </div>
                        <div className="flex items-center text-sm text-stone-500">
                            <Clock size={14} className="mr-2" />
                            {formatDateTime(order.createdAt)}
                        </div>
                    </div>
                </div>

                <div className="p-8">
                    <h3 className="font-bold mb-6 flex items-center">
                        <Package size={20} className="mr-2" />
                        주문 상품
                    </h3>
                    <div className="space-y-6 mb-10">
                        {order.items.map((item) => (
                            <div key={item.id} className="flex items-center bg-stone-50 p-4 rounded-2xl">
                                <img
                                    src={item.imageUrl || "/api/placeholder/80/80"}
                                    alt={item.productName}
                                    className="w-20 h-20 rounded-xl object-cover"
                                />
                                <div className="ml-6 flex-1">
                                    <h4 className="font-bold text-stone-900">{item.productName}</h4>
                                    <p className="text-sm text-stone-500 mt-1">{item.productDescription}</p>
                                    <div className="flex justify-between items-center mt-3">
                                        <span className="text-sm font-medium bg-white px-3 py-1 rounded-lg border border-stone-100">수량: {item.quantity}</span>
                                        <span className="font-bold">{item.totalPrice.toLocaleString()}원</span>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="grid md:grid-cols-2 gap-8 mb-10">
                        <div className="bg-stone-50 p-6 rounded-2xl">
                            <h3 className="font-bold mb-4 flex items-center">
                                <MapPin size={20} className="mr-2" />
                                배송 정보
                            </h3>
                            <div className="space-y-3 text-sm text-stone-600">
                                <p><span className="font-medium text-stone-900">수령인:</span> {order.recipientName}</p>
                                <p><span className="font-medium text-stone-900">연락처:</span> {order.recipientPhone}</p>
                                <p><span className="font-medium text-stone-900">주소:</span> {order.shippingAddress}</p>
                            </div>
                        </div>
                        <div className="bg-stone-50 p-6 rounded-2xl">
                            <h3 className="font-bold mb-4 flex items-center">
                                <CreditCard size={20} className="mr-2" />
                                결제 정보
                            </h3>
                            <div className="space-y-3 text-sm text-stone-600">
                                {order.payment ? (
                                    <>
                                        <p><span className="font-medium text-stone-900">결제수단:</span> {order.payment.paymentMethod}</p>
                                        <p><span className="font-medium text-stone-900">상태:</span> {order.payment.status}</p>
                                        <p><span className="font-medium text-stone-900">결제일시:</span> {formatDateTime(order.payment.paidAt)}</p>
                                    </>
                                ) : (
                                    <p>결제 정보가 없습니다.</p>
                                )}
                            </div>

                            {/* Cancel Button */}
                            {canCancel && !showCancelConfirm && (
                                <button
                                    onClick={() => setShowCancelConfirm(true)}
                                    className="w-full mt-6 py-4 bg-white border border-red-200 text-red-500 rounded-2xl text-sm font-bold hover:bg-red-50 transition-all"
                                >
                                    주문 취소하기
                                </button>
                            )}

                            {showCancelConfirm && (
                                <div className="mt-6 bg-red-50 p-6 rounded-2xl border border-red-200">
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
                    </div>
                </div>
            </div>
        </div>
    );
}
