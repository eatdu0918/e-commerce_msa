import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi } from '../../api/services/admin';
import {
  isCancelledOrderWithRefundComplete,
  orderStatusHeadlineLabel,
  adminNextTargetLabel,
  getNextAdminOrderStatus,
} from '../../lib/adminOrderStatus';
import {
  buildOrderCouponDetailSummary,
  getEffectiveCancelRequestTypeForDisplay,
} from '../../api/services/order';
import { Eye, Search, Filter, ChevronLeft, ChevronRight, ChevronRightCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function AdminOrderList() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['admin-orders', page],
    queryFn: () => adminApi.getOrders(page, 10),
  });

  const advanceMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      adminApi.updateOrderStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
      queryClient.invalidateQueries({ queryKey: ['admin-order-detail'] });
      alert(t('admin.order_status_updated'));
    },
    onError: () => {
      alert(t('admin.order_status_update_fail'));
    },
  });

  const orders = data?.data?.content || [];
  const pageData = data?.data;
  const dateLocale = i18n.language.startsWith('ko') ? 'ko-KR' : undefined;

  if (isError) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-red-500">{t('admin.error_load')}</div>
      </div>
    );
  }

  return (
    <div>
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold">{t('admin.nav_orders')}</h1>
          <p className="text-stone-500 text-sm mt-1">
            {t('admin.total_items', { count: pageData?.totalElements ?? 0 })}
          </p>
        </div>
      </div>

      <div className="bg-white rounded-[20px] shadow-sm border border-stone-100 overflow-hidden">
        <div className="p-5 border-b border-stone-100 flex items-center justify-between bg-stone-50/50">
          <div className="flex space-x-2">
            <div className="relative">
              <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400" />
              <input
                type="text"
                placeholder="Order #, Customer..."
                className="pl-9 pr-4 py-2 bg-white border border-stone-200 rounded-lg text-sm focus:ring-1 focus:ring-black focus:border-black outline-none w-64 transition-all"
              />
            </div>
            <button className="flex items-center px-3 py-2 bg-white border border-stone-200 rounded-lg text-sm text-stone-600 hover:bg-stone-50 transition-colors">
              <Filter size={16} className="mr-2" />
              Filter
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="bg-stone-50 text-stone-500 font-medium border-b border-stone-100">
              <tr>
                <th className="px-6 py-4">Order ID</th>
                <th className="px-6 py-4">Date</th>
                <th className="px-6 py-4">Customer</th>
                <th className="px-6 py-4">Total</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4 text-right">{t('admin.actions')}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-stone-50">
              {isLoading ? (
                [...Array(5)].map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={6} className="px-6 py-4">
                      <div className="h-10 bg-stone-100 rounded-lg" />
                    </td>
                  </tr>
                ))
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-stone-400">
                    No orders found
                  </td>
                </tr>
              ) : (
                orders.map((order) => {
                  const displayStatus = (order.progressStatus || order.status || '').toUpperCase();
                  const nextCode = getNextAdminOrderStatus(order.status, {
                    paymentStatus: order.paymentStatus,
                    activeCancelStatus: order.activeCancelStatus,
                    progressStatus: order.progressStatus,
                    skipConfirmAndPreparing: order.skipConfirmAndPreparing,
                    skipShippingAndDelivered: order.skipShippingAndDelivered,
                  });
                  const nextLabel = nextCode ? adminNextTargetLabel(t, nextCode) : null;
                  const discount = Number(order.discountAmount ?? 0);
                  const finalPrice =
                    order.finalAmount != null && !Number.isNaN(Number(order.finalAmount))
                      ? Number(order.finalAmount)
                      : Number(order.totalAmount ?? 0);
                  const listCouponDetail = buildOrderCouponDetailSummary(t, order);
                  return (
                  <tr key={order.id} className="hover:bg-stone-50/50 transition-colors relative group">
                    <td className="px-6 py-4 font-medium">#{order.id}</td>
                    <td className="px-6 py-4 text-stone-500">
                      {new Date(order.createdAt).toLocaleDateString(dateLocale)}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center">
                        <div className="w-6 h-6 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-[10px] font-bold mr-2">
                          U
                        </div>
                        <span>{order.recipientName || `User #${order.userId}`}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-bold tabular-nums">₩{finalPrice.toLocaleString()}</div>
                      {discount > 0 && (
                        <div className="text-[11px] text-stone-500 mt-1 space-y-0.5">
                          <div className="line-through tabular-nums">
                            ₩{Number(order.totalAmount).toLocaleString()}
                          </div>
                          <div className="text-rose-600 font-medium tabular-nums">
                            {t('orderList.list_discount_hint', {
                              amount: discount.toLocaleString(),
                            })}
                          </div>
                          {listCouponDetail ? (
                            <div className="text-stone-600 leading-tight max-w-[200px]">
                              {t('admin.order_coupon_applied', { detail: listCouponDetail })}
                            </div>
                          ) : null}
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          displayStatus === 'DELIVERED'
                            ? 'bg-emerald-100 text-emerald-800'
                            : isCancelledOrderWithRefundComplete(displayStatus, order.paymentStatus)
                              ? 'bg-emerald-100 text-emerald-800'
                              : displayStatus === 'CANCELLED'
                                ? 'bg-red-100 text-red-800'
                                : displayStatus === 'CANCEL_REQUESTED'
                                  ? 'bg-amber-100 text-amber-900'
                                  : displayStatus === 'CONFIRMED' ||
                                      displayStatus === 'PREPARING' ||
                                      displayStatus === 'SHIPPING'
                                    ? 'bg-green-100 text-green-800'
                                    : 'bg-blue-100 text-blue-800'
                        }`}
                      >
                        {orderStatusHeadlineLabel(
                          t,
                          displayStatus,
                          order.paymentStatus,
                          getEffectiveCancelRequestTypeForDisplay(order)
                        )}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <div className="inline-flex items-center justify-end gap-1">
                        {nextCode && nextLabel && (
                          <button
                            type="button"
                            title={t('admin.order_next_step', { label: nextLabel })}
                            onClick={() => {
                              if (
                                window.confirm(
                                  t('admin.order_confirm_advance', { label: nextLabel })
                                )
                              ) {
                                advanceMutation.mutate({ id: order.id, status: nextCode });
                              }
                            }}
                            disabled={advanceMutation.isPending}
                            className="p-2 text-stone-500 hover:text-emerald-700 hover:bg-emerald-50 rounded-lg transition-colors disabled:opacity-50"
                          >
                            <ChevronRightCircle size={18} />
                          </button>
                        )}
                        <button
                          type="button"
                          onClick={() => navigate(`/admin/orders/${order.id}`)}
                          className="p-2 text-stone-400 hover:text-black hover:bg-stone-100 rounded-lg transition-colors"
                          aria-label={t('admin.order_detail_open')}
                        >
                          <Eye size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        <div className="flex items-center justify-between px-6 py-4 border-t border-stone-100 bg-stone-50/50">
          <button
            type="button"
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={isLoading || pageData?.first}
            className="flex items-center px-4 py-2 text-sm font-medium text-stone-700 bg-white border border-stone-200 rounded-lg hover:bg-stone-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ChevronLeft className="w-4 h-4 mr-1" />
            {t('admin.prev')}
          </button>
          <span className="text-sm text-stone-700">
            {page + 1} / {pageData?.totalPages || 1}
          </span>
          <button
            type="button"
            onClick={() => setPage((p) => p + 1)}
            disabled={isLoading || pageData?.last}
            className="flex items-center px-4 py-2 text-sm font-medium text-stone-700 bg-white border border-stone-200 rounded-lg hover:bg-stone-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {t('admin.next')}
            <ChevronRight className="w-4 h-4 ml-1" />
          </button>
        </div>
      </div>
    </div>
  );
}
