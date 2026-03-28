/** DB 주문 status 기준 관리자 배송·준비 단계(백엔드 전이와 동일). */
const NEXT_BY_STATUS: Record<string, string | null> = {
  PENDING: 'CONFIRMED',
  CONFIRMED: 'PREPARING',
  PREPARING: 'SHIPPING',
  SHIPPING: 'DELIVERED',
  DELIVERED: null,
  CANCELLED: null,
  CANCEL_REQUESTED: null,
};

export type AdminFulfillmentContext = {
  paymentStatus?: string | null;
  activeCancelStatus?: string | null;
};

/** 취소·환불(결제 취소/환불)·진행 중 취소가 있으면 배송 단계 진행 불가 */
export function isAdminFulfillmentAdvanceBlocked(input: {
  dbStatus: string | undefined | null;
  paymentStatus?: string | null;
  activeCancelStatus?: string | null;
}): boolean {
  const db = (input.dbStatus || '').toUpperCase();
  if (db === 'CANCELLED' || db === 'CANCEL_REQUESTED') return true;
  if (input.activeCancelStatus != null && String(input.activeCancelStatus).trim() !== '') {
    return true;
  }
  const ps = (input.paymentStatus || '').trim().toUpperCase();
  return ps === 'CANCELLED' || ps === 'REFUNDED';
}

export function getNextAdminOrderStatus(
  dbStatus: string | undefined | null,
  ctx?: AdminFulfillmentContext
): string | null {
  if (
    isAdminFulfillmentAdvanceBlocked({
      dbStatus,
      paymentStatus: ctx?.paymentStatus,
      activeCancelStatus: ctx?.activeCancelStatus,
    })
  ) {
    return null;
  }
  if (!dbStatus) return null;
  const key = dbStatus.toUpperCase();
  return NEXT_BY_STATUS[key] ?? null;
}

export const ADMIN_FULFILLMENT_STEPS = [
  'PENDING',
  'CONFIRMED',
  'PREPARING',
  'SHIPPING',
  'DELIVERED',
] as const;

/**
 * 사용자 주문 상세(OrderDetailView getOrderStepperDisplay)와 동일 규칙.
 * CONFIRMED(배지: 주문 확인)일 때까지는 '결제 완료' 칸까지 완료 체크로 보이게 함.
 */
export function getAdminFulfillmentStepperDisplay(statusKey: string): {
  completedThrough: number;
  pulseAt: number | null;
} {
  const last = ADMIN_FULFILLMENT_STEPS.length - 1;
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
}

/** 스테퍼 라벨 (사용자 주문 상세와 동일하게 CONFIRMED 단계 문구 사용) */
export function adminStepperStepLabel(t: (key: string) => string, step: string): string {
  if (step === 'CONFIRMED') return t('orderStatus.CONFIRMED_LIST');
  return t(`orderStatus.${step}`);
}

/** 배지·요약용 상태 문구 */
export function adminDisplayStatusLabel(
  t: (key: string) => string,
  statusKey: string
): string {
  const key = statusKey.toUpperCase();
  const map: Record<string, string> = {
    PENDING: t('orderStatus.PENDING'),
    CONFIRMED: t('orderStatus.CONFIRMED_BADGE'),
    PREPARING: t('orderStatus.PREPARING'),
    SHIPPING: t('orderStatus.SHIPPING'),
    DELIVERED: t('orderStatus.DELIVERED'),
    CANCELLED: t('orderStatus.CANCELLED'),
    CANCEL_REQUESTED: t('orderStatus.CANCEL_REQUESTED_DETAIL'),
  };
  return map[key] ?? statusKey;
}

/**
 * 주문 DB/진행 상태가 취소여도 PG 환불까지 끝나면 화면 헤드라인은 「환불 완료」 우선.
 */
export function orderStatusHeadlineLabel(
  t: (key: string) => string,
  displayStatusKey: string,
  paymentStatus?: string | null
): string {
  const s = (displayStatusKey || '').toUpperCase();
  const pay = (paymentStatus ?? '').trim().toUpperCase();
  if (s === 'CANCELLED' && pay === 'REFUNDED') {
    return t('paymentHistory.status_REFUNDED');
  }
  return adminDisplayStatusLabel(t, displayStatusKey);
}

export function isCancelledOrderWithRefundComplete(
  displayStatusKey: string,
  paymentStatus?: string | null
): boolean {
  const s = (displayStatusKey || '').toUpperCase();
  const pay = (paymentStatus ?? '').trim().toUpperCase();
  return s === 'CANCELLED' && pay === 'REFUNDED';
}

/** 다음 단계로 바꿀 때 안내 문구 */
export function adminNextTargetLabel(t: (key: string) => string, nextCode: string): string {
  if (nextCode === 'CONFIRMED') return t('orderStatus.CONFIRMED_BADGE');
  return t(`orderStatus.${nextCode}`);
}
