import { useMemo } from 'react';
import { Users, ShoppingCart, CreditCard, Ticket, RefreshCw } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi, type Order } from '../../api/services/admin';
import { isCancelledOrderWithRefundComplete, orderStatusHeadlineLabel } from '../../lib/adminOrderStatus';
import { buildOrderCouponDetailSummary, getEffectiveCancelRequestTypeForDisplay } from '../../api/services/order';

const AdminDashboard = () => {
  const { t } = useTranslation();

  const formatRelativeTime = (dateStr: string) => {
    const diff = Date.now() - new Date(dateStr).getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return t('admin.time_just');
    if (minutes < 60) return t('admin.time_minutes_ago', { count: minutes });
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return t('admin.time_hours_ago', { count: hours });
    const days = Math.floor(hours / 24);
    return t('admin.time_days_ago', { count: days });
  };

  const { data: usersData, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-stats-users'],
    queryFn: () => adminApi.getUsers(0, 1),
    staleTime: 30000,
  });

  const { data: ordersData, isLoading: ordersLoading } = useQuery({
    queryKey: ['admin-stats-orders'],
    queryFn: () => adminApi.getOrders(0, 1),
    staleTime: 30000,
  });

  const { data: paymentsData, isLoading: paymentsLoading } = useQuery({
    queryKey: ['admin-stats-payments'],
    queryFn: () => adminApi.getPayments(0, 1, 'COMPLETED'),
    staleTime: 30000,
  });

  const { data: couponsData, isLoading: couponsLoading } = useQuery({
    queryKey: ['admin-stats-coupons'],
    queryFn: () => adminApi.getCoupons(0, 1),
    staleTime: 30000,
  });

  const { data: recentOrdersData, isLoading: recentLoading } = useQuery({
    queryKey: ['admin-recent-orders'],
    queryFn: () => adminApi.getOrders(0, 5),
    staleTime: 30000,
  });

  const formatCount = (value: number | undefined, loading: boolean) => {
    if (loading) return '...';
    if (value === undefined) return '-';
    return value.toLocaleString();
  };

  const stats = useMemo(
    () => [
      {
        title: t('admin.metric_users'),
        value: formatCount(usersData?.data?.totalElements, usersLoading),
        icon: Users,
        bgColor: 'bg-blue-100',
        iconColor: 'text-blue-600',
      },
      {
        title: t('admin.metric_orders'),
        value: formatCount(ordersData?.data?.totalElements, ordersLoading),
        icon: ShoppingCart,
        bgColor: 'bg-green-100',
        iconColor: 'text-green-600',
      },
      {
        title: t('admin.metric_payments'),
        value: formatCount(paymentsData?.data?.totalElements, paymentsLoading),
        icon: CreditCard,
        bgColor: 'bg-purple-100',
        iconColor: 'text-purple-600',
      },
      {
        title: t('admin.metric_coupons'),
        value: formatCount(couponsData?.data?.totalElements, couponsLoading),
        icon: Ticket,
        bgColor: 'bg-orange-100',
        iconColor: 'text-orange-600',
      },
    ],
    [usersData, ordersData, paymentsData, couponsData, usersLoading, ordersLoading, paymentsLoading, couponsLoading, t]
  );

  const recentOrders = recentOrdersData?.data?.content ?? [];

  const orderRowMeta = (order: Order) => {
    const displayStatus = (order.progressStatus || order.status || '').toUpperCase();
    const discount = Number(order.discountAmount ?? 0);
    const finalPrice =
      order.finalAmount != null && !Number.isNaN(Number(order.finalAmount))
        ? Number(order.finalAmount)
        : Number(order.totalAmount ?? 0);
    const couponDetail = buildOrderCouponDetailSummary(t, order);
    const effectiveCancel = getEffectiveCancelRequestTypeForDisplay(order);
    const headline = orderStatusHeadlineLabel(t, displayStatus, order.paymentStatus, effectiveCancel);
    return { displayStatus, discount, finalPrice, couponDetail, headline };
  };

  const statusBadgeClass = (order: Order, displayStatus: string) => {
    return displayStatus === 'DELIVERED'
      ? 'bg-emerald-100 text-emerald-800'
      : isCancelledOrderWithRefundComplete(
            displayStatus,
            order.paymentStatus,
            getEffectiveCancelRequestTypeForDisplay(order)
          )
        ? 'bg-emerald-100 text-emerald-800'
        : displayStatus === 'CANCELLED'
          ? 'bg-red-100 text-red-800'
          : displayStatus === 'CANCEL_REQUESTED'
            ? 'bg-amber-100 text-amber-900'
            : displayStatus === 'CONFIRMED' ||
                displayStatus === 'PREPARING' ||
                displayStatus === 'SHIPPING'
              ? 'bg-green-100 text-green-800'
              : 'bg-blue-100 text-blue-800';
  };

  return (
    <div>
      <h2 className="text-2xl font-bold text-gray-900 mb-6">{t('admin.dashboard_title')}</h2>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div key={stat.title} className="bg-white rounded-lg shadow p-6 border border-gray-200">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                  <p className="text-3xl font-bold text-gray-900 mt-2">{stat.value}</p>
                </div>
                <div className={`${stat.bgColor} p-3 rounded-lg`}>
                  <Icon className={`w-6 h-6 ${stat.iconColor}`} />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-8 bg-white rounded-lg shadow p-6 border border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">{t('admin.recent_orders')}</h3>
        {recentLoading ? (
          <div className="flex items-center justify-center py-8 text-gray-400">
            <RefreshCw className="w-5 h-5 animate-spin mr-2" />
            {t('admin.loading_orders')}
          </div>
        ) : recentOrders.length === 0 ? (
          <p className="text-center py-8 text-gray-400">{t('admin.no_recent_orders')}</p>
        ) : (
          <div className="space-y-4">
            {recentOrders.map((order, index) => {
              const { displayStatus, discount, finalPrice, couponDetail, headline } = orderRowMeta(order);
              return (
                <div
                  key={order.id}
                  className={`flex items-start justify-between gap-4 py-3 ${
                    index < recentOrders.length - 1 ? 'border-b border-gray-100' : ''
                  }`}
                >
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-medium text-gray-900">{t('admin.order_row', { id: order.id })}</p>
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusBadgeClass(
                          order,
                          displayStatus
                        )}`}
                      >
                        {headline}
                      </span>
                    </div>
                    <div className="mt-1 text-sm text-gray-600">
                      <span className="font-medium tabular-nums text-gray-900">
                        {finalPrice.toLocaleString()}
                        {t('common.currency_won')}
                      </span>
                      {discount > 0 && (
                        <span className="ml-2 text-gray-500">
                          <span className="line-through tabular-nums">
                            {Number(order.totalAmount).toLocaleString()}
                            {t('common.currency_won')}
                          </span>
                          <span className="ml-2 text-rose-600 font-medium tabular-nums">
                            {t('orderList.list_discount_hint', { amount: discount.toLocaleString() })}
                          </span>
                        </span>
                      )}
                    </div>
                    {couponDetail ? (
                      <p className="mt-1 text-xs text-gray-500 leading-snug">
                        {t('admin.order_coupon_applied', { detail: couponDetail })}
                      </p>
                    ) : null}
                  </div>
                  <span className="shrink-0 text-sm text-gray-500">{formatRelativeTime(order.createdAt)}</span>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
