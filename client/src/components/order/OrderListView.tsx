import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ShoppingBag, ChevronRight, Package, Truck, CheckCircle2, AlertCircle, RefreshCw, Layers } from 'lucide-react';
import type { LucideIcon } from 'lucide-react';
import { getMyOrders } from '../../api/services/order';
import type { OrderResponse } from '../../api/services/order';
import { useTranslation } from 'react-i18next';
import { formatDateTime, getEstimatedArrivalDate } from '../../utils/date';
import { useEffect } from 'react';

interface StatusTheme {
    color: string;
    bg: string;
    icon: LucideIcon;
    label: string;
}

const STATUS_THEME: Record<string, StatusTheme> = {
    PENDING: { color: 'text-amber-600', bg: 'bg-amber-50', icon: AlertCircle, label: '주문 접수' },
    CONFIRMED: { color: 'text-blue-600', bg: 'bg-blue-50', icon: RefreshCw, label: '결제 완료' },
    PREPARING: { color: 'text-indigo-600', bg: 'bg-indigo-50', icon: Package, label: '상품 준비' },
    SHIPPING: { color: 'text-purple-600', bg: 'bg-purple-50', icon: Truck, label: '배송 중' },
    DELIVERED: { color: 'text-emerald-600', bg: 'bg-emerald-50', icon: CheckCircle2, label: '배송 완료' },
    CANCELLED: { color: 'text-rose-600', bg: 'bg-rose-50', icon: AlertCircle, label: '주문 취소' },
    CANCEL_REQUESTED: { color: 'text-stone-500', bg: 'bg-stone-100', icon: RefreshCw, label: '취소 요청' },
};

const FALLBACK_IMAGES = {
    watch: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&q=80&w=400&h=400',
    headphone: 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&q=80&w=400&h=400',
    shoe: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&q=80&w=400&h=400',
    sneaker: 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?auto=format&fit=crop&q=80&w=400&h=400',
    camera: 'https://images.unsplash.com/photo-1585333127302-e29f1163398c?auto=format&fit=crop&q=80&w=400&h=400',
    laptop: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&q=80&w=400&h=400',
    default: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&q=80&w=400&h=400'
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

export default function OrderListView() {
    const { t } = useTranslation();
    const navigate = useNavigate();

    useEffect(() => {
        console.log('OrderListView v3.1: Premium UI Loaded');
    }, []);

    const { data: ordersPage, isLoading } = useQuery({
        queryKey: ['orders'],
        queryFn: () => getMyOrders(0, 20),
    });

    const orders = ordersPage?.content || [];

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-20">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
            </div>
        );
    }

    if (orders.length === 0) {
        return (
            <div className="text-center py-24 bg-stone-50 rounded-[40px] border border-dashed border-stone-200">
                <div className="mx-auto w-16 h-16 bg-stone-100 rounded-full flex items-center justify-center text-stone-300 mb-6">
                    <ShoppingBag size={24} />
                </div>
                <h3 className="text-lg font-bold text-stone-900 mb-2">{t('order.no_history')}</h3>
                <p className="text-stone-400 mb-8 max-w-xs mx-auto text-sm">아직 주문하신 내역이 없습니다. 다양한 상품들을 구경해보세요.</p>
                <button
                    onClick={() => navigate('/shop')}
                    className="px-8 py-3 bg-black text-white rounded-full font-bold text-sm hover:opacity-80 transition-all active:scale-95"
                >
                    {t('order.go_shopping')}
                </button>
            </div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto px-4 py-8">
            <div className="space-y-8 pb-12">
                <div className="flex items-center justify-between mb-2">
                    <h2 className="text-3xl font-black italic tracking-tighter uppercase text-stone-900">{t('order.title')}</h2>
                    <div className="bg-stone-100 px-3 py-1 rounded-full">
                        <span className="text-[10px] font-black text-stone-500 uppercase tracking-widest">Total {orders.length}</span>
                    </div>
                </div>
                
                <div className="grid gap-8">
                    {orders.map((order: OrderResponse) => {
                        const theme = STATUS_THEME[order.status] || STATUS_THEME.PENDING;
                        const StatusIcon = theme.icon;
                        const items = order.items || [];
                        const firstItem = items.length > 0 ? items[0] : null;
                        const itemName = firstItem?.productName || `주문 번호 ${order.orderNumber}`;
                        const etcCount = items.length > 1 ? items.length - 1 : 0;
                        
                        // Use the purchased image from the first item if available
                        const imgUrl = (firstItem?.imageUrl && firstItem.imageUrl !== '') 
                            ? firstItem.imageUrl 
                            : getFallbackImage(itemName);

                        return (
                            <div
                                key={order.id}
                                className="group bg-white border border-stone-100 rounded-[2.5rem] p-8 hover:shadow-2xl hover:shadow-stone-200/50 transition-all duration-700 cursor-pointer overflow-hidden relative"
                                onClick={() => navigate(`/me/orders/${order.id}`)}
                            >
                                <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 mb-8">
                                    <div className="flex items-center gap-4">
                                        <div className={`w-12 h-12 rounded-2xl ${theme.bg} ${theme.color} flex items-center justify-center`}>
                                            <StatusIcon size={24} />
                                        </div>
                                        <div>
                                            <span className={`text-[10px] font-black uppercase tracking-[0.2em] ${theme.color}`}>
                                                {theme.label}
                                            </span>
                                            <div className="flex items-center gap-3 mt-1">
                                                <span className="text-sm font-black text-stone-900">
                                                    Order #{order.orderNumber}
                                                </span>
                                                <span className="text-xs text-stone-300 font-bold">
                                                    {formatDateTime(order.createdAt).split(' ')[0]}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <button className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-stone-400 group-hover:text-black transition-colors bg-stone-50 md:bg-transparent px-6 py-3 md:px-0 md:py-0 rounded-full">
                                        Track Order
                                        <ChevronRight size={14} className="group-hover:translate-x-1 transition-transform" />
                                    </button>
                                </div>

                                <div className="flex flex-col sm:flex-row items-start gap-8">
                                    <div className="relative w-full sm:w-32 h-40 sm:h-32 bg-stone-50 rounded-3xl overflow-hidden flex-shrink-0 flex items-center justify-center border border-stone-100 shadow-inner group-hover:border-stone-200 transition-colors">
                                        <img 
                                            src={imgUrl} 
                                            alt={itemName}
                                            className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-1000 opacity-90"
                                        />
                                        <div className="absolute inset-0 bg-gradient-to-t from-black/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />
                                    </div>
                                    
                                    <div className="flex-1 min-w-0">
                                        <h4 className="text-2xl font-black text-stone-900 mb-2 truncate group-hover:text-stone-700 transition-colors">
                                            {itemName}{etcCount > 0 ? ` 외 ${etcCount}건` : ''}
                                        </h4>
                                        <div className="flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-6">
                                            <span className="text-2xl font-light text-stone-900">
                                                {order.totalAmount.toLocaleString()}원
                                            </span>
                                            <div className="flex items-center gap-2 bg-emerald-50 text-emerald-600 px-3 py-1.5 rounded-xl border border-emerald-100">
                                                <CheckCircle2 size={14} />
                                                <span className="text-[10px] font-black uppercase tracking-widest">
                                                    {getEstimatedArrivalDate(order.createdAt)} Arrival
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="mt-8 pt-8 border-t border-stone-50 flex flex-col sm:flex-row items-center justify-between gap-4">
                                    <div className="flex items-center gap-4">
                                        <div className="flex items-center -space-x-5">
                                            {[...Array(Math.min(3, items.length || 1))].map((_, i) => (
                                                <div key={i} className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full border border-stone-200 bg-stone-100 text-[10px] font-black text-stone-400 ring-4 ring-white">
                                                    <Layers size={18} className="shrink-0" aria-hidden />
                                                </div>
                                            ))}
                                        </div>
                                        <span className="text-[10px] font-black text-stone-400 uppercase tracking-widest bg-stone-50 px-4 py-2 rounded-2xl border border-stone-100">
                                            {items.length || 0} Item{ items.length !== 1 ? 's' : '' }
                                        </span>
                                    </div>
                                    <div className="text-[11px] font-black text-stone-200 uppercase tracking-[0.3em] font-serif italic">
                                        Minimalist Luxury
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
}


