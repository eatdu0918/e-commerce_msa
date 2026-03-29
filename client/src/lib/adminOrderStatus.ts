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
  /** 집계 API progressStatus(체크아웃 단계 생략·결제 반영). DB보다 앞선 표시일 때 잘못된 진행 버튼 방지 */
  progressStatus?: string | null;
  skipConfirmAndPreparing?: boolean;
  skipShippingAndDelivered?: boolean;
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
  const display = (ctx?.progressStatus || '').toUpperCase();
  const db = dbStatus.toUpperCase();

  if (db === 'DELIVERED') {
    return null;
  }

  /*
   * 집계(progressStatus)가 이미 배송 완료면 흐름 종료 — 스킵 결제 등으로 화면만 먼저 끝난 경우에도
   * «다음: 배송 완료» 같은 불필요한 PATCH를 노출하지 않음(DB는 사가에서 맞춰짐).
   */
  if (display === 'DELIVERED') {
    return null;
  }

  /*
   * 표시는 이미 «배송 중»인데 DB가 뒤처진 경우: 다음 API 호출로 DB를 맞춤.
   * CONFIRMED + 아직 상품준비 시작 전이면 표시만 앞선 경우 → 통상 다음 단계(PREPARING)로 복귀.
   */
  if (display === 'SHIPPING' && db !== 'SHIPPING' && db !== 'DELIVERED') {
    if (db === 'PREPARING') {
      return 'SHIPPING';
    }
    /*
     * DB는 아직 PENDING인데 결제·집계만 앞선 경우(체크아웃 단계 생략 + 사가 반영 지연).
     * 백엔드에서 skip 주문에 한해 PENDING → SHIPPING 동기화를 허용한다.
     */
    if (db === 'PENDING' && ctx?.skipConfirmAndPreparing === true) {
      return 'SHIPPING';
    }
    if (db === 'CONFIRMED' && ctx?.skipConfirmAndPreparing === true) {
      return 'SHIPPING';
    }
    if (db === 'CONFIRMED') {
      return 'PREPARING';
    }
    return null;
  }

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
export type AdminHeadlineFulfillmentSkipContext = {
  skipConfirmAndPreparing?: boolean;
  skipShippingAndDelivered?: boolean;
};

export function orderStatusHeadlineLabel(
  t: (key: string) => string,
  displayStatusKey: string,
  paymentStatus?: string | null,
  /** 목록·상세 집계의 유효 요청 유형(getEffectiveCancelRequestTypeForDisplay 권장) */
  effectiveCancelRequestType?: string | null,
  /** 어드민 주문 상세: 첫 스킵만 선택 시 집계가 SHIPPING이어도 헤드라인을 「상품 준비 완료」로 표시 */
  adminFulfillmentSkipHeadline?: AdminHeadlineFulfillmentSkipContext | null
): string {
  const s = (displayStatusKey || '').toUpperCase();
  const pay = (paymentStatus ?? '').trim().toUpperCase();
  const rt = (effectiveCancelRequestType ?? '').trim().toUpperCase();
  if (
    s === 'CANCELLED' &&
    (pay === 'REFUNDED' || (pay === 'CANCELLED' && rt === 'RETURN_REFUND'))
  ) {
    return t('paymentHistory.status_REFUNDED');
  }
  if (s === 'CANCEL_REQUESTED' && rt === 'RETURN_REFUND') {
    return t('orderStatus.RETURN_REFUND_REQUESTED_DETAIL');
  }
  if (adminFulfillmentSkipHeadline) {
    const onlyFirstSkip =
      adminFulfillmentSkipHeadline.skipConfirmAndPreparing === true &&
      adminFulfillmentSkipHeadline.skipShippingAndDelivered !== true;
    if (onlyFirstSkip && s === 'SHIPPING' && pay === 'COMPLETED') {
      return t('orderStatus.PREPARING_COMPLETE_HEADLINE');
    }
  }
  return adminDisplayStatusLabel(t, displayStatusKey);
}

export function isCancelledOrderWithRefundComplete(
  displayStatusKey: string,
  paymentStatus?: string | null,
  effectiveCancelRequestType?: string | null
): boolean {
  const s = (displayStatusKey || '').toUpperCase();
  const pay = (paymentStatus ?? '').trim().toUpperCase();
  const rt = (effectiveCancelRequestType ?? '').trim().toUpperCase();
  if (s !== 'CANCELLED') return false;
  if (pay === 'REFUNDED') return true;
  return pay === 'CANCELLED' && rt === 'RETURN_REFUND';
}

/** 반품·환불(RETURN_REFUND)이 끝난 뒤 배송 단계 영역 안내 문구 분기용 */
export function isReturnRefundFlowSettledForFulfillmentHint(
  displayStatusKey: string,
  paymentStatus?: string | null,
  effectiveCancelRequestType?: string | null
): boolean {
  if (
    !isCancelledOrderWithRefundComplete(
      displayStatusKey,
      paymentStatus,
      effectiveCancelRequestType
    )
  ) {
    return false;
  }
  return (effectiveCancelRequestType ?? '').trim().toUpperCase() === 'RETURN_REFUND';
}

/**
 * 추정 도착일·빠른 배송 안내 카드는 배송 완료, 주문 취소 완료, 환불(처리) 완료일 때 숨김.
 */
export function shouldShowOrderEstimatedArrival(input: {
  progressStatus: string | null | undefined;
  orderDbStatus: string | null | undefined;
  paymentStatus?: string | null | undefined;
}): boolean {
  const progress = (input.progressStatus || '').toUpperCase();
  const db = (input.orderDbStatus || '').toUpperCase();
  const pay = (input.paymentStatus ?? '').trim().toUpperCase();

  if (progress === 'DELIVERED' || db === 'DELIVERED') return false;
  if (progress === 'CANCELLED' || db === 'CANCELLED') return false;
  if (pay === 'REFUNDED') return false;

  return true;
}

/** 다음 단계로 바꿀 때 안내 문구 */
export function adminNextTargetLabel(t: (key: string) => string, nextCode: string): string {
  if (nextCode === 'CONFIRMED') return t('orderStatus.CONFIRMED_BADGE');
  return t(`orderStatus.${nextCode}`);
}
