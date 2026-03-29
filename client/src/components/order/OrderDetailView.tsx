import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { formatDateTime, getEstimatedArrivalDate } from '../../utils/date';
import { CreditCard, MapPin, Package, ArrowLeft, CheckCircle2, Truck, Receipt, Calendar, Info, ChevronRight, AlertCircle } from 'lucide-react';
import {
    buildOrderCouponDetailSummary,
    getEffectiveCancelRequestTypeForDisplay,
    getOrderDetail,
    getUiProgressStatus,
} from '../../api/services/order';
import type { OrderDetailResponse } from '../../api/services/order';
import { createCancel, CANCEL_REASON_LABELS } from '../../api/services/cancel';
import type { CancelReason, CreateCancelRequest, CancelRequestType } from '../../api/services/cancel';
import { useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { isCancelledOrderWithRefundComplete, orderStatusHeadlineLabel } from '../../lib/adminOrderStatus';

/** 출고 전 주문 취소 신청 가능 상태 */
const ORDER_CANCEL_STATUSES = ['PENDING', 'CONFIRMED', 'PREPARING'];
/** 반품·환불(배송 완료 후) */
const RETURN_REFUND_STATUSES = ['DELIVERED'];

const FALLBACK_IMAGES = {
    watch: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=400&h=400',
    headphone: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=400&h=400',
    shoe: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=400&h=400',
    sneaker: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?auto=format&fit=crop&q=80&w=400&h=400',
    camera: 'https://images.unsplash.com/photo-1585333127302-e29f1163398c?auto=format&fit=crop&q=80&w=400&h=400',
    laptop: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&q=80&w=400&h=400',
    default: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&q=80&w=400&h=400'
};

/** 마일스톤(완료 구간)과 '진행 중' 배지 위치를 분리 — 결제 완료 단계에서는 해당 칸이 '진행 중'이 되지 않음 */
const getOrderStepperDisplay = (
    statusKey: string,
    stepCount: number
): { completedThrough: number; pulseAt: number | null } => {
    const last = stepCount - 1;
    const u = (statusKey || '').toUpperCase();
    switch (u) {
        case 'PENDING':
            return { completedThrough: 0, pulseAt: 0 };
        case 'CONFIRMED':
            return { completedThrough: 1, pulseAt: Math.min(2, last) };
        case 'PREPARING':
            return { completedThrough: 2, pulseAt: Math.min(2, last) };
        case 'SHIPPING':
            return { completedThrough: 2, pulseAt: Math.min(3, last) };
        case 'DELIVERED':
            return { completedThrough: last, pulseAt: null };
        default:
            return { completedThrough: 0, pulseAt: 0 };
    }
};

const getFallbackImage = (name: string = '') => {
    const lowerName = name.toLowerCase();
    if (lowerName.includes('sneaker') || lowerName.includes('운동화')) return FALLBACK_IMAGES.sneaker;
    if (lowerName.includes('shoe') || lowerName.includes('신발') || lowerName.includes('구두')) return FALLBACK_IMAGES.shoe;
    if (lowerName.includes('watch') || lowerName.includes('시계')) return FALLBACK_IMAGES.watch;
    if (lowerName.includes('headphone') || lowerName.includes('헤드폰') || lowerName.includes('이어폰')) return FALLBACK_IMAGES.headphone;
    if (lowerName.includes('camera') || lowerName.includes('카메라')) return FALLBACK_IMAGES.camera;
    if (lowerName.includes('laptop') || lowerName.includes('노트북') || lowerName.includes('컴퓨터') || lowerName.includes('mouse') || lowerName.includes('마우스')) return FALLBACK_IMAGES.laptop;
    return FALLBACK_IMAGES.default;
};

export default function OrderDetailView() {
    const { t } = useTranslation();
    const { id } = useParams();
    const orderId = Number(id);
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [cancelReason, setCancelReason] = useState<CancelReason>('CHANGE_OF_MIND');
    const [cancelDetail, setCancelDetail] = useState('');

    const STATUS_STEPS = useMemo(
        () => [
            { key: 'PENDING', label: t('orderStatus.PENDING'), icon: Receipt },
            { key: 'CONFIRMED', label: t('orderStatus.CONFIRMED_LIST'), icon: CheckCircle2 },
            { key: 'PREPARING', label: t('orderStatus.PREPARING'), icon: Package },
            { key: 'SHIPPING', label: t('orderStatus.SHIPPING'), icon: Truck },
            { key: 'DELIVERED', label: t('orderStatus.DELIVERED'), icon: CheckCircle2 },
        ],
        [t]
    );

    const { data: order, isLoading } = useQuery<OrderDetailResponse>({
        queryKey: ['order', orderId],
        queryFn: () => getOrderDetail(orderId),
    });

    const cancelMutation = useMutation({
        mutationFn: (data: CreateCancelRequest) => createCancel(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['order', orderId] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
            queryClient.invalidateQueries({ queryKey: ['cancels'] });
            setShowCancelModal(false);
            navigate('/me/cancel-refund');
        },
    });

    const handleCancelSubmit = () => {
        if (!order) return;
        const phase = (getUiProgressStatus(order) || order.status || '').toUpperCase();
        const postDelivered = phase === 'DELIVERED';
        const requestType: CancelRequestType = postDelivered ? 'RETURN_REFUND' : 'ORDER_CANCEL';
        const request: CreateCancelRequest = {
            orderId: order.id,
            orderNumber: order.orderNumber,
            cancelReason,
            cancelDetail: cancelDetail.trim() || undefined,
            requestType,
            items: (order.items || []).map(item => ({
                productId: item.productId,
                productName: item.productName || t('orderDetail.product_unknown'),
                quantity: item.quantity || 1,
                unitPrice: item.unitPrice || 0,
            })),
        };
        cancelMutation.mutate(request);
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-32">
                <div className="relative">
                    <div className="w-12 h-12 rounded-full border-4 border-stone-100 animate-pulse"></div>
                    <div className="absolute top-0 left-0 w-12 h-12 rounded-full border-t-4 border-black animate-spin"></div>
                </div>
            </div>
        );
    }

    if (!order) {
        return (
            <div className="max-w-xl mx-auto text-center py-32 px-4">
                <div className="mx-auto w-20 h-20 bg-rose-50 rounded-full flex items-center justify-center text-rose-500 mb-6">
                    <Info size={32} />
                </div>
                <h3 className="text-xl font-bold text-stone-900 mb-2">{t('orderDetail.not_found_title')}</h3>
                <p className="text-stone-500 mb-10">{t('orderDetail.not_found_desc')}</p>
                <button 
                    onClick={() => navigate('/me/orders')} 
                    className="px-10 py-4 bg-black text-white rounded-2xl font-black text-sm hover:scale-105 active:scale-95 transition-all"
                >
                    {t('orderDetail.back_list')}
                </button>
            </div>
        );
    }

    const progressStatus = getUiProgressStatus(order);
    /** 취소 가능 여부는 스텝퍼와 동일한 단계(getUiProgressStatus). skip 플로우에서 DB status만 쓰면 배송 중인데도 취소 버튼이 뜨는 경우가 있다. */
    const fulfillmentPhaseKey = (progressStatus || order.status || '').toUpperCase();
    const effectiveCancelRequestType = getEffectiveCancelRequestTypeForDisplay(order);
    const isCancelledFlow = ['CANCELLED', 'CANCEL_REQUESTED'].includes(fulfillmentPhaseKey);
    const { completedThrough: stepperCompletedThrough, pulseAt: stepperPulseAt } = getOrderStepperDisplay(
        progressStatus,
        STATUS_STEPS.length
    );
    const stepperLineThroughIndex =
        stepperPulseAt != null
            ? Math.max(stepperCompletedThrough, stepperPulseAt)
            : stepperCompletedThrough;
    const blockingCancelStatuses = ['REQUESTED', 'APPROVED', 'COMPLETED'];
    const hasActiveCancelPipeline =
        !!order.activeCancelStatus && blockingCancelStatuses.includes(order.activeCancelStatus);
    const rejectedOrderCancel = !!order.hasRejectedOrderCancelRequest;
    const rejectedReturnRefund = !!order.hasRejectedReturnRefundRequest;
    const canRequestOrderCancel =
        ORDER_CANCEL_STATUSES.includes(fulfillmentPhaseKey) &&
        fulfillmentPhaseKey !== 'CANCEL_REQUESTED' &&
        !hasActiveCancelPipeline &&
        !rejectedOrderCancel;
    const canRequestReturnRefund =
        RETURN_REFUND_STATUSES.includes(fulfillmentPhaseKey) &&
        fulfillmentPhaseKey !== 'CANCEL_REQUESTED' &&
        !hasActiveCancelPipeline &&
        !rejectedReturnRefund;
    const canCancel = canRequestOrderCancel || canRequestReturnRefund;
    const isPostShipmentReturn = fulfillmentPhaseKey === 'DELIVERED';
    const orderIsCancelled = order.status === 'CANCELLED' || progressStatus === 'CANCELLED';
    const orderItems = order.items || [];
    const headerStatusKey = fulfillmentPhaseKey;
    const headlineStatusLabel =
        orderStatusHeadlineLabel(
            t,
            headerStatusKey,
            order.payment?.status,
            effectiveCancelRequestType
        ) || order.statusDescription;

    const discountNum = Number(order.discountAmount ?? 0);
    const totalNum = Number(order.totalAmount ?? 0);
    const finalNum =
        order.finalAmount != null && !Number.isNaN(Number(order.finalAmount))
            ? Number(order.finalAmount)
            : Math.max(0, totalNum - discountNum);
    const couponSummaryLine = buildOrderCouponDetailSummary(t, order);
    const discountSubLabel =
        discountNum > 0 && !couponSummaryLine ? t('orderDetail.discount_matched_payment') : null;

    return (
        <div className="max-w-5xl mx-auto px-4 py-12">
            <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 mb-10">
                <div className="flex items-center gap-4">
                    <button
                        onClick={() => navigate('/me/orders')}
                        className="w-12 h-12 flex items-center justify-center bg-white border border-stone-100 rounded-2xl text-stone-900 hover:bg-black hover:text-white hover:border-black transition-all group active:scale-95 shadow-sm"
                    >
                        <ArrowLeft size={20} className="group-hover:-translate-x-1 transition-transform" />
                    </button>
                    <div>
                        <h2 className="text-2xl font-black tracking-tighter uppercase italic">{t('orderDetail.title')}</h2>
                        <p className="text-stone-400 text-xs font-bold mt-0.5">#{order.orderNumber}</p>
                    </div>
                </div>
                
                <div className="flex items-center gap-3">
                    <div className="flex flex-col items-end">
                        <span className="text-[10px] font-black text-stone-400 uppercase tracking-widest">{t('orderDetail.order_placed')}</span>
                        <span className="text-sm font-bold text-stone-900">{formatDateTime(order.createdAt).split(' ')[0]}</span>
                    </div>
                    <div className="h-8 w-px bg-stone-100 mx-1"></div>
                    <span className={`px-5 py-2.5 rounded-2xl text-xs font-black uppercase tracking-widest ${
                        headerStatusKey === 'DELIVERED' ? 'bg-emerald-50 text-emerald-600' :
                        isCancelledOrderWithRefundComplete(headerStatusKey, order.payment?.status)
                            ? 'bg-emerald-50 text-emerald-600' :
                        headerStatusKey === 'CANCEL_REQUESTED' ? 'bg-amber-50 text-amber-800' :
                        headerStatusKey === 'CANCELLED' ? 'bg-rose-50 text-rose-600' :
                        'bg-blue-50 text-blue-600'
                    }`}>
                        {headlineStatusLabel}
                    </span>
                </div>
            </header>

            {headerStatusKey === 'CANCEL_REQUESTED' && (
                <div
                    className="mb-10 flex gap-4 rounded-[2rem] border border-amber-200/80 bg-amber-50/90 p-6 text-amber-950 shadow-sm"
                    role="status"
                >
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-amber-100 text-amber-700">
                        <AlertCircle size={22} />
                    </div>
                    <div>
                        <p className="text-sm font-black uppercase tracking-tight">
                            {(effectiveCancelRequestType ?? '').toUpperCase() === 'RETURN_REFUND'
                                ? t('orderDetail.return_pending_notice')
                                : t('orderDetail.cancel_pending_notice')}
                        </p>
                        <p className="mt-1 text-xs font-medium leading-relaxed text-amber-900/80">
                            {(effectiveCancelRequestType ?? '').toUpperCase() === 'RETURN_REFUND'
                                ? t('orderDetail.return_pending_hint')
                                : t('orderDetail.cancel_pending_hint')}
                        </p>
                    </div>
                </div>
            )}

            {/* Visual Status Stepper */}
            {!isCancelledFlow && (
                <div className="bg-white rounded-[2.5rem] border border-stone-100 p-8 mb-10 shadow-sm overflow-hidden border-b-4 border-b-black/5">
                    <div className="flex flex-col sm:flex-row justify-between items-start gap-8 relative">
                        {/* Connection Line (Desktop) */}
                        <div className="hidden sm:block absolute top-[1.35rem] left-[2.5rem] right-[2.5rem] h-0.5 bg-stone-100 -z-0">
                            <div 
                                className="h-full bg-black transition-all duration-1000 ease-out" 
                                style={{
                                    width: `${(stepperLineThroughIndex / Math.max(1, STATUS_STEPS.length - 1)) * 100}%`,
                                }}
                            />
                        </div>

                        {STATUS_STEPS.map((step, index) => {
                            const isCompleted = index <= stepperCompletedThrough;
                            const isCurrent = stepperPulseAt !== null && index === stepperPulseAt;
                            const Icon = step.icon;

                            const iconClass =
                                isCompleted
                                    ? `bg-black text-white shadow-lg shadow-black/20${
                                          isCurrent ? ' ring-4 ring-black/10 scale-110' : ''
                                      }`
                                    : isCurrent
                                      ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30 ring-4 ring-blue-500/30 scale-110'
                                      : 'bg-stone-50 text-stone-300';

                            const labelClass =
                                isCompleted
                                    ? 'text-black'
                                    : isCurrent
                                      ? 'text-blue-700'
                                      : 'text-stone-300';

                            return (
                                <div key={step.key} className="flex sm:flex-col items-center gap-4 sm:gap-3 flex-1 relative z-10 w-full sm:w-auto">
                                    <div className={`w-11 h-11 rounded-2xl flex items-center justify-center transition-all duration-500 ${iconClass}`}>
                                        <Icon size={index === 4 && isCompleted ? 20 : 18} />
                                    </div>
                                    <div className="flex flex-col sm:items-center">
                                        <span className={`text-[11px] font-black uppercase tracking-tighter ${labelClass}`}>
                                            {step.label}
                                        </span>
                                        {isCurrent && (
                                            <span className="text-[9px] font-bold text-blue-600 animate-pulse">{t('orderDetail.current_step')}</span>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            <div className="grid lg:grid-cols-5 gap-10">
                {/* Left Side: Order Items */}
                <div className="lg:col-span-3 space-y-8">
                    <section>
                        <div className="flex items-center justify-between mb-6">
                            <h3 className="text-xl font-black tracking-tighter uppercase italic flex items-center">
                                <Package size={20} className="mr-3" />
                                {t('orderDetail.items_title')}
                            </h3>
                            <span className="text-xs font-bold text-stone-400">{t('orderDetail.items_count', { count: orderItems.length })}</span>
                        </div>
                        
                        <div className="space-y-4">
                            {orderItems.map((item) => (
                                <div
                                    key={item.id}
                                    role="button"
                                    tabIndex={0}
                                    className="group flex items-center bg-white border border-stone-100 rounded-3xl p-4 hover:shadow-xl hover:shadow-stone-200/50 transition-all duration-500 cursor-pointer"
                                    onClick={() => {
                                        if (item.productId != null && item.productId > 0) {
                                            navigate(`/product/${item.productId}`);
                                        }
                                    }}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter' || e.key === ' ') {
                                            e.preventDefault();
                                            if (item.productId != null && item.productId > 0) {
                                                navigate(`/product/${item.productId}`);
                                            }
                                        }
                                    }}
                                >
                                    <div className="relative w-24 h-24 rounded-2xl overflow-hidden flex-shrink-0 bg-stone-50 border border-stone-100">
                                        <img
                                            src={item.imageUrl && item.imageUrl !== '' ? item.imageUrl : getFallbackImage(item.productName)}
                                            alt={item.productName || 'product'}
                                            onError={(e) => {
                                                const target = e.target as HTMLImageElement;
                                                target.src = getFallbackImage(item.productName);
                                            }}
                                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-700"
                                        />
                                    </div>
                                    <div className="ml-6 flex-1 min-w-0">
                                        <div className="flex justify-between items-start">
                                            <div>
                                                <h4 className="font-bold text-stone-900 text-lg mb-1 truncate">{item.productName}</h4>
                                                <p className="text-xs text-stone-400 line-clamp-1">{item.productDescription}</p>
                                            </div>
                                            <ChevronRight size={16} className="text-stone-200 group-hover:text-black group-hover:translate-x-1 transition-all" />
                                        </div>
                                        <div className="flex justify-between items-center mt-4">
                                            <div className="flex items-center gap-2">
                                                <span className="px-2.5 py-1 rounded-lg bg-stone-50 border border-stone-100 text-[10px] font-black uppercase tracking-wider text-stone-500">
                                                    {t('orderDetail.qty_badge', { count: item.quantity || 1 })}
                                                </span>
                                                <span className="text-xs text-stone-300 font-medium tracking-tight">
                                                    × {(item.unitPrice || 0).toLocaleString()}
                                                    {t('common.currency_won')}
                                                </span>
                                            </div>
                                            <span className="font-black text-stone-900">
                                                {(item.totalPrice || (item.unitPrice * item.quantity) || 0).toLocaleString()}
                                                {t('common.currency_won')}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                            {orderItems.length === 0 && (
                                <div className="p-12 text-center bg-stone-50 rounded-3xl border border-stone-100">
                                    <Package size={32} className="mx-auto text-stone-200 mb-4" />
                                    <p className="text-stone-400 text-sm font-bold uppercase tracking-widest">{t('orderDetail.no_items')}</p>
                                </div>
                            )}
                        </div>
                    </section>

                    <section className="bg-stone-900 rounded-[2.5rem] p-10 text-white relative overflow-hidden group">
                        <div className="relative z-10">
                            <h3 className="text-xl font-black tracking-tighter uppercase italic mb-8 flex items-center text-white/90">
                                <Receipt size={20} className="mr-3" />
                                {t('orderDetail.total_payment')}
                            </h3>
                            <div className="space-y-4 mb-8">
                                <div className="flex justify-between text-sm text-white/50">
                                    <span>{t('checkout.product_amount')}</span>
                                    <span>
                                        {totalNum.toLocaleString()}
                                        {t('common.currency_won')}
                                    </span>
                                </div>
                                <div className="flex justify-between text-sm text-white/50 items-start gap-4">
                                    <div className="min-w-0">
                                        <span>{t('orderDetail.discount_amount')}</span>
                                        {(couponSummaryLine || discountSubLabel) && (
                                            <p className="text-[11px] text-white/35 font-medium mt-1 leading-snug">
                                                {couponSummaryLine ?? discountSubLabel}
                                            </p>
                                        )}
                                    </div>
                                    <span className="text-rose-400 shrink-0 tabular-nums">
                                        -{discountNum.toLocaleString()}
                                        {t('common.currency_won')}
                                    </span>
                                </div>
                                <div className="flex justify-between text-sm text-white/50">
                                    <span>{t('checkout.shipping_fee')}</span>
                                    <span className="uppercase">{t('orderDetail.free_shipping')}</span>
                                </div>
                                <div className="h-px bg-white/10 my-2"></div>
                                <div className="flex justify-between items-end">
                                    <span className="text-sm font-bold opacity-80 uppercase tracking-widest">{t('orderDetail.final_total')}</span>
                                    <span className="text-4xl font-black italic tracking-tighter">
                                        {finalNum.toLocaleString()}
                                        {t('common.currency_won')}
                                    </span>
                                </div>
                            </div>
                        </div>
                        {/* Decorative elements */}
                        <div className="absolute -bottom-8 -right-8 w-40 h-40 bg-white/5 rounded-full blur-3xl group-hover:scale-150 transition-transform duration-1000"></div>
                        <div className="absolute top-8 right-8 text-[8rem] font-black text-white/[0.03] leading-none select-none -z-0 italic uppercase tracking-tighter">
                            Total
                        </div>
                    </section>
                </div>

                {/* Right Side: Shipping & Payment info */}
                <div className="lg:col-span-2 space-y-8">
                    <section className="bg-white rounded-[2.5rem] border border-stone-100 p-8 shadow-sm">
                        <h3 className="text-lg font-black tracking-tighter uppercase italic mb-6 flex items-center">
                            <MapPin size={20} className="mr-3" />
                            {t('orderDetail.shipping_title')}
                        </h3>
                        <div className="space-y-6">
                            <div className="bg-stone-50/50 rounded-2xl p-5 border border-stone-100 hover:border-black/10 transition-all">
                                <div className="flex items-center gap-3 mb-3">
                                    <div className="w-8 h-8 rounded-xl bg-white border border-stone-100 flex items-center justify-center">
                                        <Info size={14} className="text-stone-400" />
                                    </div>
                                    <span className="text-[10px] font-black uppercase tracking-widest text-stone-400">{t('orderDetail.recipient_details')}</span>
                                </div>
                                <p className="text-sm font-black text-stone-900 mb-1">{order.recipientName}</p>
                                <p className="text-xs text-stone-500 font-medium tracking-tight mb-4">{order.recipientPhone}</p>
                                
                                <div className="p-3 bg-white rounded-xl border border-stone-100">
                                    <div className="flex items-center gap-2 mb-1.5">
                                        <MapPin size={12} className="text-stone-400" />
                                        <span className="text-[9px] font-black uppercase tracking-widest text-stone-300">{t('orderDetail.delivery_address')}</span>
                                    </div>
                                    <p className="text-xs text-stone-600 leading-relaxed font-medium">
                                        {order.shippingAddress}
                                    </p>
                                </div>
                            </div>

                            <div className="bg-emerald-50/50 rounded-2xl p-5 border border-emerald-100/50 flex items-start gap-4">
                                <div className="p-2 bg-emerald-500 text-white rounded-lg">
                                    <Calendar size={16} />
                                </div>
                                <div>
                                    <span className="text-[10px] font-black uppercase tracking-widest text-emerald-600 block mb-1">{t('orderDetail.estimated_arrival')}</span>
                                    <p className="text-sm font-black text-stone-900">
                                        {t('orderDetail.arrival_by', { date: getEstimatedArrivalDate(order.createdAt) })}
                                    </p>
                                    <p className="text-[10px] text-emerald-600/70 font-bold mt-0.5 select-none animate-pulse">{t('orderDetail.express_note')}</p>
                                </div>
                            </div>
                        </div>
                    </section>

                    <section className="bg-white rounded-[2.5rem] border border-stone-100 p-8 shadow-sm">
                        <h3 className="text-lg font-black tracking-tighter uppercase italic mb-6 flex items-center">
                            <CreditCard size={20} className="mr-3" />
                            {t('orderDetail.payment_title')}
                        </h3>
                        {order.payment ? (
                            <div className="space-y-4">
                                {orderIsCancelled && (
                                    <div
                                        className="rounded-2xl border border-rose-200 bg-rose-50/90 p-4 text-sm font-bold text-rose-900"
                                        role="status"
                                    >
                                        {t('orderDetail.order_cancelled_notice')}
                                    </div>
                                )}
                                <div
                                    className={`flex items-center justify-between p-4 rounded-2xl border ${
                                        orderIsCancelled
                                            ? 'border-stone-200 bg-stone-50/80 opacity-95'
                                            : 'border-stone-100 bg-stone-50'
                                    }`}
                                >
                                    <div className="flex items-center gap-3">
                                        <div className="w-10 h-10 rounded-xl bg-white border border-stone-100 flex items-center justify-center font-black italic tracking-tighter text-stone-900">
                                            {(order.payment?.paymentMethod || 'PAY').substring(0, 2).toUpperCase()}
                                        </div>
                                        <div>
                                            <p className="text-xs font-black uppercase tracking-tighter text-stone-900">
                                                {(() => {
                                                    const details = order.payment?.paymentDetails;
                                                    if (!details) {
                                                        return order.payment?.paymentMethod || t('paymentDisplay.no_method');
                                                    }

                                                    try {
                                                        const parsed = JSON.parse(details);
                                                        if (parsed.card) {
                                                            const code = String(parsed.card.issuerCode ?? '');
                                                            const issuer = t(`paymentIssuer.${code}`, {
                                                                defaultValue:
                                                                    parsed.card.issuerCode || t('paymentDisplay.card_fallback'),
                                                            });
                                                            const installment =
                                                                parsed.card.installmentPlanMonths === 0
                                                                    ? t('paymentDisplay.single_pay')
                                                                    : t('paymentDisplay.installment', {
                                                                          months: parsed.card.installmentPlanMonths,
                                                                      });
                                                            return `${issuer} (${installment})`;
                                                        }
                                                        if (parsed.easyPay) {
                                                            return t('paymentDisplay.easy_pay', {
                                                                provider: parsed.easyPay.provider,
                                                            });
                                                        }
                                                        return details;
                                                    } catch {
                                                        return details;
                                                    }
                                                })()}
                                            </p>
                                            <p
                                                className={`text-[10px] font-bold uppercase ${
                                                    orderIsCancelled ? 'text-rose-600' : 'text-stone-400'
                                                }`}
                                            >
                                                {(order.payment?.status || '').toUpperCase() === 'COMPLETED' &&
                                                orderIsCancelled
                                                    ? t('orderDetail.payment_status_order_cancelled')
                                                    : (order.payment?.status || '').toUpperCase() === 'CANCELLED'
                                                      ? t('orderDetail.payment_status_cancelled')
                                                      : order.payment?.status}
                                            </p>
                                        </div>
                                    </div>
                                    <div className="text-right">
                                        <p className="text-[10px] text-stone-400 font-bold uppercase mb-0.5 tracking-widest">{t('orderDetail.paid_date')}</p>
                                        <p className="text-xs font-black text-stone-900 tracking-tighter">
                                            {order.payment?.paidAt ? formatDateTime(order.payment.paidAt).split(' ')[0] : '-'}
                                        </p>
                                    </div>
                                </div>
                            </div>
                        ) : (
                            <div className="py-4 text-center border-2 border-dashed border-stone-100 rounded-3xl">
                                <p className="text-xs font-bold text-stone-300 uppercase tracking-widest">{t('orderDetail.no_payment')}</p>
                            </div>
                        )}

                        {rejectedOrderCancel &&
                            ORDER_CANCEL_STATUSES.includes(fulfillmentPhaseKey) &&
                            !hasActiveCancelPipeline && (
                                <p
                                    className="mt-6 rounded-2xl border border-stone-200/80 bg-stone-50/90 px-4 py-3 text-center text-xs font-medium text-stone-600"
                                    role="status"
                                >
                                    {t('orderDetail.order_cancel_blocked_after_rejection')}
                                </p>
                            )}
                        {rejectedReturnRefund &&
                            RETURN_REFUND_STATUSES.includes(fulfillmentPhaseKey) &&
                            !hasActiveCancelPipeline && (
                                <p
                                    className="mt-6 rounded-2xl border border-rose-100 bg-rose-50/80 px-4 py-3 text-center text-xs font-medium text-rose-800"
                                    role="status"
                                >
                                    {t('orderDetail.return_blocked_after_rejection')}
                                </p>
                            )}
                        {canCancel && (
                            <button
                                onClick={() => setShowCancelModal(true)}
                                className="w-full mt-10 py-5 bg-white border border-stone-100 text-rose-500 rounded-[2rem] text-xs font-black uppercase tracking-widest hover:bg-rose-50 hover:border-rose-100 hover:scale-[1.02] active:scale-95 transition-all shadow-sm"
                            >
                                {isPostShipmentReturn
                                    ? t('orderDetail.request_return_refund')
                                    : t('orderDetail.request_cancel')}
                            </button>
                        )}
                    </section>
                </div>
            </div>

            {/* Cancel Request Modal */}
            {showCancelModal && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 transition-all duration-500 animate-in fade-in">
                    <div className="bg-white rounded-[3rem] max-w-md w-full p-10 shadow-2xl animate-in zoom-in-95 duration-300">
                        <div className="flex items-center gap-4 mb-8">
                            <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-500 flex items-center justify-center">
                                <AlertCircle size={24} />
                            </div>
                            <div>
                                <h3 className="font-black text-xl tracking-tighter uppercase italic">
                                    {isPostShipmentReturn
                                        ? t('orderDetail.return_refund_title')
                                        : t('orderDetail.cancel_title')}
                                </h3>
                                <p className="text-stone-400 text-[10px] font-black uppercase tracking-widest">
                                    {isPostShipmentReturn
                                        ? t('orderDetail.return_refund_subtitle')
                                        : t('orderDetail.cancel_subtitle')}
                                </p>
                            </div>
                        </div>

                        <div className="mb-8">
                            <label className="block text-xs font-black uppercase tracking-widest text-stone-400 mb-4">
                                {isPostShipmentReturn
                                    ? t('orderDetail.return_reason_label')
                                    : t('orderDetail.cancel_reason_label')}
                            </label>
                            <div className="space-y-2">
                                {(Object.keys(CANCEL_REASON_LABELS) as CancelReason[]).map((value) => (
                                    <label
                                        key={value}
                                        className={`flex items-center p-4 rounded-2xl border cursor-pointer transition-all duration-300 ${cancelReason === value
                                            ? 'border-black bg-stone-50 ring-4 ring-black/5'
                                            : 'border-stone-100 hover:border-stone-200 hover:bg-stone-50/50'
                                            }`}
                                    >
                                        <input
                                            type="radio"
                                            name="cancelReason"
                                            value={value}
                                            checked={cancelReason === value}
                                            onChange={() => setCancelReason(value)}
                                            className="sr-only"
                                        />
                                        <div className={`w-5 h-5 rounded-full border-2 mr-4 flex items-center justify-center transition-colors ${cancelReason === value ? 'border-black' : 'border-stone-200'
                                            }`}>
                                            {cancelReason === value && <div className="w-2.5 h-2.5 bg-black rounded-full" />}
                                        </div>
                                        <span className={`text-sm font-bold ${cancelReason === value ? 'text-black' : 'text-stone-500'}`}>
                                            {t(`cancelReason.${value}` as const)}
                                        </span>
                                    </label>
                                ))}
                            </div>
                        </div>

                        <div className="mb-8">
                            <label className="block text-xs font-black uppercase tracking-widest text-stone-400 mb-3">{t('orderDetail.cancel_optional')}</label>
                            <textarea
                                value={cancelDetail}
                                onChange={(e) => setCancelDetail(e.target.value)}
                                placeholder={t('orderDetail.cancel_placeholder')}
                                rows={3}
                                className="w-full border border-stone-100 rounded-2xl p-4 text-sm font-medium focus:outline-none focus:ring-4 focus:ring-black/5 focus:border-black/20 transition-all resize-none placeholder:text-stone-300"
                            />
                        </div>

                        <div className="flex gap-4">
                            <button
                                onClick={handleCancelSubmit}
                                disabled={cancelMutation.isPending}
                                className="flex-[2] py-4 bg-rose-500 text-white rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-rose-600 hover:scale-[1.02] active:scale-95 transition-all disabled:opacity-50 shadow-lg shadow-rose-200"
                            >
                                {cancelMutation.isPending ? t('orderDetail.cancel_processing') : t('orderDetail.cancel_confirm')}
                            </button>
                            <button
                                onClick={() => {
                                    setShowCancelModal(false);
                                    setCancelReason('CHANGE_OF_MIND');
                                    setCancelDetail('');
                                }}
                                className="flex-1 py-4 bg-stone-50 text-stone-900 rounded-2xl text-xs font-black uppercase tracking-widest hover:bg-stone-100 transition-all active:scale-95"
                            >
                                {t('orderDetail.cancel_close')}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

