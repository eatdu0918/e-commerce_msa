import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ShoppingBag } from 'lucide-react';
import { getMyOrders } from '../../api/services/order';
import { useTranslation } from 'react-i18next';

export default function OrderListView() {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const { data: ordersPage } = useQuery({
        queryKey: ['orders'],
        queryFn: () => getMyOrders(0, 20),
    });

    const orders = ordersPage?.content || [];

    if (orders.length === 0) {
        return (
            <div className="text-center py-20 text-stone-500">
                <p className="mb-4">{t('order.no_history')}</p>
                <button onClick={() => navigate('/shop')} className="text-black underline font-bold">
                    {t('order.go_shopping')}
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-12 text-left">
            {orders.map((order: any) => (
                <div
                    key={order.id}
                    className="border-b border-stone-100 pb-12 last:border-0 last:pb-0 cursor-pointer hover:bg-stone-50/50 -mx-4 px-4 py-2 rounded-2xl transition-colors"
                    onClick={() => navigate(`/me/orders/${order.id}`)}
                >
                    <div className="flex items-center justify-between mb-6">
                        <span className="text-xs font-bold text-stone-400 uppercase tracking-widest">
                            {t('order.order_number')} {order.orderNumber}
                        </span>
                        <span className="text-xs bg-blue-50 text-blue-600 px-3 py-1 rounded-full font-bold">
                            {order.status}
                        </span>
                    </div>
                    <div className="flex space-x-8 items-center">
                        <div className="w-32 h-32 bg-stone-100 rounded-3xl overflow-hidden flex items-center justify-center text-stone-300">
                            <ShoppingBag size={32} />
                        </div>
                        <div>
                            <h4 className="text-xl font-bold mb-2">
                                {t('order.total')} {order.totalAmount.toLocaleString()}원
                            </h4>
                            <p className="text-stone-400 text-sm mb-4">
                                {t('order.items_etc', { id: order.id })
                                    .replace('Item', '상품')
                                    .replace('etc...', '외...')}
                            </p>
                        </div>
                    </div>
                    <div className="pt-8 flex justify-between items-center text-sm">
                        <span className="text-stone-500">
                            {t('order.arrival_date', { date: '12월 14일' })}
                        </span>
                        <span className="text-stone-900 font-bold border-b border-black">
                            {t('order.shipping_detail')}
                        </span>
                    </div>
                </div>
            ))}
        </div>
    );
}
