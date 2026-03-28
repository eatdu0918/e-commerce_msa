import { useEffect, useRef, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createOrder } from '../../api/services/order';
import { createPayment } from '../../api/services/payment';
import type { CreateOrderRequest, OrderItemRequest } from '../../api/services/order';
import { Check, Loader2 } from 'lucide-react';
import { getApiErrorMessage } from '../../lib/getApiErrorMessage';

export default function PaymentSuccessPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [error, setError] = useState('');
    const [processing, setProcessing] = useState(true);
    const processedPaymentKeyRef = useRef<string | null>(null);

    const paymentKey = searchParams.get('paymentKey');
    const orderId = searchParams.get('orderId');
    const amount = searchParams.get('amount');

    const orderMutation = useMutation({
        mutationFn: async () => {
            // localStorage에서 저장된 주문 정보 가져오기
            const savedOrderData = localStorage.getItem('pendingOrderData');
            if (!savedOrderData) {
                throw new Error('주문 정보를 찾을 수 없습니다.');
            }

            const orderData: {
                items: OrderItemRequest[];
                shippingAddress: string;
                recipientName: string;
                recipientPhone: string;
                userCouponId?: number;
                paymentMethod: string;
            } = JSON.parse(savedOrderData);

            const orderRequest: CreateOrderRequest = {
                items: orderData.items,
                shippingAddress: orderData.shippingAddress,
                recipientName: orderData.recipientName,
                recipientPhone: orderData.recipientPhone,
                ...(orderData.userCouponId ? { userCouponId: orderData.userCouponId } : {}),
            };

            const order = await createOrder(orderRequest);

            // 결제 기록 생성
            // 실제 서비스에서는 여기서 백엔드의 '결제 승인 API'를 호출해야 하며, 
            // 백엔드에서 Toss API를 통해 받은 상세 응답(issuerCode, installmentPlanMonths 등)을 
            // paymentDetails에 JSON으로 저장해야 합니다.
            // 여기서는 UI 시연을 위해 모의 상세 데이터를 저장합니다.
            const mockPaymentDetails = JSON.stringify({
                method: orderData.paymentMethod,
                card: {
                    issuerCode: 'HYUNDAI', // 현대카드
                    installmentPlanMonths: 0, // 일시불
                    number: '44445555****8888',
                    cardType: '신용',
                    ownerType: '개인'
                }
            });

            await createPayment({
                orderId: order.id,
                orderNumber: order.orderNumber,
                paymentMethod: orderData.paymentMethod,
                amount: order.finalAmount,
                paymentDetails: mockPaymentDetails,
            });

            // 주문 정보 삭제
            localStorage.removeItem('pendingOrderData');

            return order;
        },
        onSuccess: (order) => {
            queryClient.invalidateQueries({ queryKey: ['cart'] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
            setProcessing(false);
            // 주문 완료 페이지로 이동
            setTimeout(() => {
                navigate(`/me/orders/${order.id}`, { replace: true });
            }, 1500);
        },
        onError: (err: unknown) => {
            setError(getApiErrorMessage(err, '주문 처리 중 오류가 발생했습니다.'));
            setProcessing(false);
        },
    });

    useEffect(() => {
        if (!paymentKey || !orderId || !amount) {
            setError('결제 정보가 올바르지 않습니다.');
            setProcessing(false);
            return;
        }
        if (processedPaymentKeyRef.current === paymentKey) {
            return;
        }
        processedPaymentKeyRef.current = paymentKey;
        orderMutation.mutate();
        // 동일 paymentKey당 1회만 실행 (Strict Mode 이중 effect 방지)
        // eslint-disable-next-line react-hooks/exhaustive-deps -- mutate는 안정 참조
    }, [paymentKey, orderId, amount]);

    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#f9f7f2]">
                <div className="bg-white p-10 rounded-3xl shadow-sm border border-stone-100 max-w-md w-full text-center">
                    <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6">
                        <span className="text-3xl">❌</span>
                    </div>
                    <h2 className="text-xl font-bold mb-3">주문 처리 실패</h2>
                    <p className="text-sm text-stone-500 mb-6">{error}</p>
                    <button
                        onClick={() => navigate('/shop', { replace: true })}
                        className="bg-black text-white px-8 py-3 rounded-full text-sm font-bold hover:bg-stone-800 transition-all"
                    >
                        쇼핑으로 돌아가기
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-[#f9f7f2]">
            <div className="bg-white p-10 rounded-3xl shadow-sm border border-stone-100 max-w-md w-full text-center">
                {processing ? (
                    <>
                        <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-6">
                            <Loader2 size={32} className="text-blue-500 animate-spin" />
                        </div>
                        <h2 className="text-xl font-bold mb-3">결제 완료! 주문을 처리하고 있습니다...</h2>
                        <p className="text-sm text-stone-500">잠시만 기다려주세요.</p>
                    </>
                ) : (
                    <>
                        <div className="w-16 h-16 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-6">
                            <Check size={32} className="text-green-500" />
                        </div>
                        <h2 className="text-xl font-bold mb-3">주문이 완료되었습니다!</h2>
                        <p className="text-sm text-stone-500">주문 상세 페이지로 이동합니다...</p>
                    </>
                )}
            </div>
        </div>
    );
}
