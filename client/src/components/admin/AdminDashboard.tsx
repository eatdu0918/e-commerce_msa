import { useMemo } from 'react';
import { Users, ShoppingCart, CreditCard, Ticket, RefreshCw } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi } from '../../api/services/admin';

const AdminDashboard = () => {
  const { t } = useTranslation();

  const orderStatusLabel = useMemo(() => {
    const keys = ['PENDING', 'CONFIRMED', 'PAYMENT_COMPLETED', 'CANCELLED', 'COMPLETED'] as const;
    const map: Record<string, string> = {};
    keys.forEach((k) => {
      map[k] = t(`admin.order_dash_${k}`);
    });
    return map;
  }, [t]);

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
            {recentOrders.map((order, index) => (
              <div
                key={order.id}
                className={`flex items-center justify-between py-3 ${index < recentOrders.length - 1 ? 'border-b border-gray-100' : ''
                  }`}
              >
                <div>
                  <p className="font-medium text-gray-900">{t('admin.order_row', { id: order.id })}</p>
                  <p className="text-sm text-gray-500">
                    {orderStatusLabel[order.progressStatus || order.status] ??
                      order.progressStatus ??
                      order.status}
                    {' · '}
                    {order.totalAmount.toLocaleString()}
                    {t('common.currency_won')}
                  </p>
                </div>
                <span className="text-sm text-gray-500">{formatRelativeTime(order.createdAt)}</span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;
