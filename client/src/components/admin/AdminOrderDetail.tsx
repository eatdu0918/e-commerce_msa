import { useParams, Link } from 'react-router-dom';
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { adminApi } from '../../api/services/admin';
import {
  ADMIN_FULFILLMENT_STEPS,
  adminNextTargetLabel,
  adminStepperStepLabel,
  getNextAdminOrderStatus,
  getAdminFulfillmentStepperDisplay,
  isAdminFulfillmentAdvanceBlocked,
  orderStatusHeadlineLabel,
} from '../../lib/adminOrderStatus';
import {
  buildOrderCouponDetailSummary,
  getEffectiveCancelRequestTypeForDisplay,
} from '../../api/services/order';
import { ArrowLeft, Check, Package, Truck, CircleDot, X } from 'lucide-react';

type OrderLineForAllocate = { totalPrice?: number; quantity?: number };

/**
 * 주문 최종 결제액을 품목별 totalPrice 비중으로 나눈 표시용 정수 원(합계가 final과 일치).
 */
function allocateFinalAmountToLines(
  items: OrderLineForAllocate[],
  orderTotalAmount: number,
  orderFinalAmount: number
): { lineTotal: number; unitPrice: number }[] | null {
  if (!items.length || orderTotalAmount <= 0) return null;
  const target = Math.max(0, Math.round(Number(orderFinalAmount)));
  const basisTotal = Math.round(Number(orderTotalAmount));
  if (target >= basisTotal) return null;

  const linePres = items.map((it) =>
    Math.max(0, Math.round(Number(it.totalPrice ?? 0)))
  );
  const sumLines = linePres.reduce((a, b) => a + b, 0);
  if (sumLines <= 0) return null;

  const exact = linePres.map((t) => (t / sumLines) * target);
  const rounded = exact.map((x) => Math.floor(x));
  let gap = target - rounded.reduce((a, b) => a + b, 0);
  const byFrac = exact
    .map((x, i) => ({ i, f: x - Math.floor(x) }))
    .sort((a, b) => b.f - a.f);
  for (let k = 0; k < byFrac.length && gap > 0; k++) {
    rounded[byFrac[k].i] += 1;
    gap -= 1;
  }

  return items.map((it, i) => {
    const qty = Math.max(1, Number(it.quantity) || 1);
    const lineTotal = rounded[i];
    const unitPrice = Math.round(lineTotal / qty);
    return { lineTotal, unitPrice };
  });
}

export default function AdminOrderDetail() {
  const { id } = useParams<{ id: string }>();
  const orderId = Number(id);
  const { t, i18n } = useTranslation();
  const queryClient = useQueryClient();
  const dateLocale = i18n.language.startsWith('ko') ? 'ko-KR' : undefined;

  const {
    data: wrap,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['admin-order-detail', orderId],
    queryFn: () => adminApi.getOrderDetail(orderId),
    enabled: Number.isFinite(orderId) && orderId > 0,
  });

  const order = wrap?.data;

  const [rejectOpen, setRejectOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  const activeCancelNorm = (order?.activeCancelStatus ?? '').toString().toUpperCase();
  const actionableCancelId =
    order?.activeCancelId != null && Number.isFinite(Number(order.activeCancelId))
      ? Number(order.activeCancelId)
      : null;
  const dbStatusUpper = (order?.status ?? '').toString().toUpperCase();
  const statusBeforeCancel = (order?.statusBeforeCancelRequest ?? '')
    .toString()
    .toUpperCase();
  const cancelAdminBlockedByShipping =
    dbStatusUpper === 'SHIPPING' ||
    (dbStatusUpper === 'CANCEL_REQUESTED' && statusBeforeCancel === 'SHIPPING');
  const showCancelActions =
    activeCancelNorm === 'REQUESTED' &&
    actionableCancelId !== null &&
    !cancelAdminBlockedByShipping;

  const updateMutation = useMutation({
    mutationFn: (next: string) => adminApi.updateOrderStatus(orderId, next),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-order-detail', orderId] });
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
      alert(t('admin.order_status_updated'));
    },
    onError: () => {
      alert(t('admin.order_status_update_fail'));
    },
  });

  const approveCancelMutation = useMutation({
    mutationFn: (cancelId: number) => adminApi.approveCancel(cancelId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-order-detail', orderId] });
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
      queryClient.invalidateQueries({ queryKey: ['admin-cancels'] });
      queryClient.invalidateQueries({ queryKey: ['admin-payments'] });
      alert(t('admin.approve_ok'));
    },
    onError: () => {
      alert(t('admin.approve_fail'));
    },
  });

  const rejectCancelMutation = useMutation({
    mutationFn: ({ cancelId, reason }: { cancelId: number; reason: string }) =>
      adminApi.rejectCancel(cancelId, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-order-detail', orderId] });
      queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
      queryClient.invalidateQueries({ queryKey: ['admin-cancels'] });
      queryClient.invalidateQueries({ queryKey: ['admin-payments'] });
      alert(t('admin.reject_ok'));
      setRejectOpen(false);
      setRejectReason('');
    },
    onError: () => {
      alert(t('admin.reject_fail'));
    },
  });

  const handleApproveCancel = () => {
    if (actionableCancelId == null) return;
    if (window.confirm(t('admin.confirm_approve'))) {
      approveCancelMutation.mutate(actionableCancelId);
    }
  };

  const handleRejectCancel = () => {
    if (actionableCancelId == null) return;
    if (!rejectReason.trim()) {
      alert(t('admin.reject_reason_required'));
      return;
    }
    rejectCancelMutation.mutate({ cancelId: actionableCancelId, reason: rejectReason.trim() });
  };

  if (!Number.isFinite(orderId) || orderId <= 0) {
    return (
      <div className="text-center py-16 text-stone-500">{t('admin.order_detail_invalid')}</div>
    );
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64 text-stone-500">
        {t('admin.loading')}
      </div>
    );
  }

  if (isError || !order) {
    return (
      <div className="flex flex-col items-center justify-center h-64 gap-4">
        <p className="text-red-600">{t('admin.order_detail_error')}</p>
        <Link to="/admin/orders" className="text-sm underline text-stone-700">
          {t('admin.order_back_list')}
        </Link>
      </div>
    );
  }

  const dbStatus = (order.status || '').toUpperCase();
  const displayKey = (order.progressStatus || order.status || '').toUpperCase();
  const payStatus = order.payment?.status ?? null;
  const paymentNorm = (payStatus ?? '').trim().toUpperCase();
  const blockedByCancelOrRefund = isAdminFulfillmentAdvanceBlocked({
    dbStatus: order.status,
    paymentStatus: payStatus,
    activeCancelStatus: order.activeCancelStatus,
  });
  const nextStatus = getNextAdminOrderStatus(order.status, {
    paymentStatus: payStatus,
    activeCancelStatus: order.activeCancelStatus,
    progressStatus: order.progressStatus,
    skipConfirmAndPreparing: order.skipConfirmAndPreparing,
    skipShippingAndDelivered: order.skipShippingAndDelivered,
  });
  const nextLabel = nextStatus ? adminNextTargetLabel(t, nextStatus) : null;
  /** DB 또는 집계 표시가 종료면 단계 진행 안내·버튼 없음 */
  const deliveryClosed =
    dbStatus === 'CANCELLED' ||
    dbStatus === 'CANCEL_REQUESTED' ||
    dbStatus === 'DELIVERED' ||
    displayKey === 'DELIVERED';

  const cancelledLike =
    dbStatus === 'CANCELLED' || dbStatus === 'CANCEL_REQUESTED' || blockedByCancelOrRefund;

  const { completedThrough, pulseAt } = getAdminFulfillmentStepperDisplay(displayKey);
  const lastStepIdx = ADMIN_FULFILLMENT_STEPS.length - 1;

  const adminCouponDetail = buildOrderCouponDetailSummary(t, order);
  const totalNum = Number(order.totalAmount ?? 0);
  const finalNum =
    order.finalAmount != null && !Number.isNaN(Number(order.finalAmount))
      ? Number(order.finalAmount)
      : totalNum;
  const itemPayDisplay = allocateFinalAmountToLines(order.items ?? [], totalNum, finalNum);

  const handleAdvance = () => {
    if (!nextStatus || !nextLabel) return;
    if (
      !window.confirm(
        t('admin.order_confirm_advance', {
          label: nextLabel,
        })
      )
    ) {
      return;
    }
    updateMutation.mutate(nextStatus);
  };

  return (
    <div className="max-w-4xl mx-auto pb-12">
      <Link
        to="/admin/orders"
        className="inline-flex items-center gap-1 text-sm text-stone-600 hover:text-black mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        {t('admin.order_back_list')}
      </Link>

      <div className="bg-white rounded-[20px] shadow-sm border border-stone-100 overflow-hidden">
        <div className="p-6 border-b border-stone-100 flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold">
              {t('admin.order_detail_title', { id: order.id })}
            </h1>
            {order.orderNumber && (
              <p className="text-sm text-stone-500 mt-1 font-mono">{order.orderNumber}</p>
            )}
            <p className="text-sm text-stone-500 mt-2">
              {new Date(order.createdAt).toLocaleString(dateLocale)}
            </p>
          </div>
          <div className="text-right space-y-2">
            <p className="text-xs uppercase tracking-wide text-stone-500">
              {t('admin.status')}
            </p>
            <p className="text-lg font-semibold text-stone-900">
              {orderStatusHeadlineLabel(
                t,
                displayKey,
                payStatus,
                getEffectiveCancelRequestTypeForDisplay(order)
              )}
            </p>
            {order.activeCancelStatus && paymentNorm !== 'REFUNDED' && (
              <p className="text-xs text-amber-800 bg-amber-50 px-2 py-1 rounded-md inline-block">
                {t('admin.order_active_cancel', { status: order.activeCancelStatus })}
              </p>
            )}
          </div>
        </div>

        {showCancelActions && actionableCancelId !== null && (
          <div className="px-6 py-4 bg-amber-50/80 border-b border-amber-100">
            <p className="text-sm text-amber-950 font-medium">
              {t('admin.order_cancel_pending_panel', { id: actionableCancelId })}
            </p>
            <div className="mt-3 flex flex-wrap items-center gap-2">
              <button
                type="button"
                onClick={handleApproveCancel}
                disabled={approveCancelMutation.isPending || rejectCancelMutation.isPending}
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-emerald-600 text-white text-sm font-medium hover:bg-emerald-700 disabled:opacity-50"
              >
                <Check className="w-4 h-4" />
                {t('admin.approve_title')}
              </button>
              <button
                type="button"
                onClick={() => setRejectOpen(true)}
                disabled={approveCancelMutation.isPending || rejectCancelMutation.isPending}
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl border border-red-200 bg-white text-red-700 text-sm font-medium hover:bg-red-50 disabled:opacity-50"
              >
                <X className="w-4 h-4" />
                {t('admin.reject_title')}
              </button>
              <Link
                to={
                  getEffectiveCancelRequestTypeForDisplay(order) === 'RETURN_REFUND'
                    ? '/admin/returns'
                    : '/admin/cancels'
                }
                className="text-sm text-amber-900 underline underline-offset-2 ml-1"
              >
                {t('admin.order_cancel_open_list')}
              </Link>
            </div>
          </div>
        )}

        <div className="p-6 border-b border-stone-100 bg-stone-50/40">
          <p className="text-sm font-medium text-stone-700 mb-4">
            {t('admin.order_fulfillment_steps')}
          </p>
          {cancelledLike ? (
            <p className="text-sm text-red-700">
              {t('admin.order_fulfillment_cancel_refund_block')}
            </p>
          ) : (
            <div className="flex flex-wrap gap-2 justify-between items-start">
              {ADMIN_FULFILLMENT_STEPS.map((step, idx) => {
                const done = idx <= completedThrough;
                const current =
                  pulseAt !== null && idx === pulseAt && idx > completedThrough;
                const labelEmphasized =
                  (pulseAt !== null && idx === pulseAt) ||
                  (pulseAt === null && idx === lastStepIdx);
                const label = adminStepperStepLabel(t, step);
                return (
                  <div
                    key={step}
                    className="flex-1 min-w-[72px] flex flex-col items-center gap-2"
                  >
                    <div
                      className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold border-2 transition-colors ${
                        done
                          ? 'bg-emerald-500 border-emerald-500 text-white'
                          : current
                            ? 'bg-white border-black text-black'
                            : 'bg-white border-stone-200 text-stone-400'
                      }`}
                    >
                      {done ? <Check className="w-5 h-5" /> : idx + 1}
                    </div>
                    <span
                      className={`text-[11px] text-center leading-tight px-1 ${
                        labelEmphasized ? 'font-semibold text-stone-900' : 'text-stone-500'
                      }`}
                    >
                      {label}
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        <div className="p-6 flex flex-wrap items-center justify-between gap-4 border-b border-stone-100">
          <div>
            <p className="text-sm text-stone-600">
              {blockedByCancelOrRefund
                ? t('admin.order_fulfillment_cancel_refund_block')
                : deliveryClosed
                  ? t('admin.order_fulfillment_done')
                  : t('admin.order_fulfillment_hint')}
            </p>
          </div>
          {nextStatus && nextLabel && (
            <button
              type="button"
              onClick={handleAdvance}
              disabled={updateMutation.isPending}
              className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-black text-white text-sm font-medium hover:bg-stone-800 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              <Truck className="w-4 h-4" />
              {t('admin.order_next_step', { label: nextLabel })}
            </button>
          )}
        </div>

        <div className="p-6 grid gap-6 md:grid-cols-2">
          <div>
            <h2 className="text-sm font-semibold text-stone-800 mb-3 flex items-center gap-2">
              <Package className="w-4 h-4" />
              {t('admin.order_recipient_section')}
            </h2>
            <dl className="text-sm space-y-2 text-stone-700">
              <div>
                <dt className="text-stone-500">{t('admin.user_id')}</dt>
                <dd>{order.userId}</dd>
              </div>
              {order.recipientName && (
                <div>
                  <dt className="text-stone-500">{t('auth.name')}</dt>
                  <dd>{order.recipientName}</dd>
                </div>
              )}
              {order.recipientPhone && (
                <div>
                  <dt className="text-stone-500">{t('auth.phone')}</dt>
                  <dd>{order.recipientPhone}</dd>
                </div>
              )}
              {order.shippingAddress && (
                <div>
                  <dt className="text-stone-500">{t('auth.address')}</dt>
                  <dd className="whitespace-pre-wrap">{order.shippingAddress}</dd>
                </div>
              )}
            </dl>
          </div>
          <div>
            <h2 className="text-sm font-semibold text-stone-800 mb-3 flex items-center gap-2">
              <CircleDot className="w-4 h-4" />
              {t('admin.order_amount_section')}
            </h2>
            <dl className="text-sm space-y-2 text-stone-700">
              <div className="flex justify-between">
                <dt className="text-stone-500">{t('common.total')}</dt>
                <dd className="font-semibold">
                  ₩{Number(order.totalAmount).toLocaleString()}
                </dd>
              </div>
              {order.discountAmount != null && Number(order.discountAmount) > 0 && (
                <div className="space-y-1">
                  <div className="flex justify-between gap-4">
                    <dt className="text-stone-500 shrink-0">{t('admin.order_discount')}</dt>
                    <dd className="text-rose-600 font-medium tabular-nums">
                      -₩{Number(order.discountAmount).toLocaleString()}
                    </dd>
                  </div>
                  {adminCouponDetail ? (
                    <p className="text-xs text-stone-500 pl-0 leading-snug">
                      {t('admin.order_coupon_applied', { detail: adminCouponDetail })}
                    </p>
                  ) : (
                    <p className="text-xs text-stone-500 pl-0 leading-snug">
                      {t('orderDetail.discount_matched_payment')}
                    </p>
                  )}
                </div>
              )}
              {order.finalAmount != null && (
                <div className="flex justify-between border-t border-stone-100 pt-2 mt-2">
                  <dt className="font-medium">{t('admin.order_final')}</dt>
                  <dd className="font-bold">
                    ₩{Number(order.finalAmount).toLocaleString()}
                  </dd>
                </div>
              )}
            </dl>
            {order.payment && (
              <div className="mt-4 p-4 rounded-xl bg-stone-50 border border-stone-100 text-sm">
                <p className="font-medium text-stone-800 mb-2">
                  {t('admin.nav_payments')}
                </p>
                <p className="text-stone-600">
                  {t('admin.payment_method')}: {order.payment.paymentMethod}
                </p>
                <p className="text-stone-600">
                  {t('admin.status')}: {order.payment.status}
                </p>
                <p className="text-stone-600">
                  ₩{Number(order.payment.amount).toLocaleString()}
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="p-6 border-t border-stone-100">
          <h2 className="text-sm font-semibold text-stone-800 mb-4">{t('admin.order_items')}</h2>
          <ul className="divide-y divide-stone-100">
            {order.items?.map((item, idx) => {
              const pay = itemPayDisplay?.[idx];
              const unitRaw = item.unitPrice ?? item.productPrice ?? 0;
              const lineRaw = Number(item.totalPrice);
              const unit = pay != null ? pay.unitPrice : Number(unitRaw);
              const lineTotal = pay != null ? pay.lineTotal : lineRaw;
              return (
                <li key={item.id} className="py-3 flex gap-4">
                  {item.imageUrl && (
                    <img
                      src={item.imageUrl}
                      alt=""
                      className="w-16 h-16 object-cover rounded-lg border border-stone-100"
                    />
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-stone-900 truncate">{item.productName}</p>
                    <p className="text-xs text-stone-500">
                      ₩{unit.toLocaleString(dateLocale)} × {item.quantity}
                    </p>
                  </div>
                  <p className="font-semibold text-stone-800">
                    ₩{lineTotal.toLocaleString(dateLocale)}
                  </p>
                </li>
              );
            })}
          </ul>
        </div>
      </div>

      {rejectOpen && actionableCancelId !== null && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl p-6 w-full max-w-md shadow-xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">{t('admin.reject_modal_title')}</h3>
              <button
                type="button"
                onClick={() => {
                  setRejectOpen(false);
                  setRejectReason('');
                }}
                aria-label={t('admin.cancel_detail_close')}
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder={t('admin.reject_placeholder')}
              className="w-full px-3 py-2 border border-stone-200 rounded-lg h-32 resize-none text-sm"
            />
            <div className="flex gap-3 mt-4">
              <button
                type="button"
                onClick={() => {
                  setRejectOpen(false);
                  setRejectReason('');
                }}
                className="flex-1 px-4 py-2 border border-stone-200 rounded-lg text-sm hover:bg-stone-50"
              >
                {t('admin.cancel_btn')}
              </button>
              <button
                type="button"
                onClick={handleRejectCancel}
                disabled={rejectCancelMutation.isPending}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg text-sm hover:bg-red-700 disabled:opacity-50"
              >
                {t('admin.reject_btn')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
