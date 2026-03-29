import api from '../axios';

export interface OrderItemRequest {
    productId: number;
    productName: string;
    imageUrl?: string;
    unitPrice: number;
    quantity: number;
}

export interface CreateOrderRequest {
    items: OrderItemRequest[];
    userCouponId?: number;
    shippingAddress: string;
    recipientName: string;
    recipientPhone: string;
    /** 결제 후 주문 확인·상품 준비 단계 생략 → 배송 중까지 즉시 */
    skipConfirmAndPreparing?: boolean;
    /** 결제 후 배송 중·배송 완료 생략 → 즉시 배송 완료 (위 옵션 true일 때만) */
    skipShippingAndDelivered?: boolean;
}

export interface OrderItemResponse {
    id: number;
    productId: number;
    productName: string;
    imageUrl?: string;
    unitPrice: number;
    quantity: number;
    totalPrice: number;
}

export interface OrderResponse {
    id: number;
    userId: number;
    orderNumber: string;
    status: string;
    /** 취소 요청 직전 주문 상태(상세·관리자 등) */
    statusBeforeCancelRequest?: string | null;
    /** 서버가 계산한 진행 표시용 상태(목록·상세 공통). PENDING+결제완료 → CONFIRMED */
    progressStatus?: string | null;
    /** 목록 API에서 결제 서비스와 통합 조회 시에만 설정 */
    paymentStatus?: string | null;
    /** 진행 중 취소 건 요청 유형: ORDER_CANCEL | RETURN_REFUND */
    /** 목록·상세 집계 시 cancel-service 요약 기준 */
    activeCancelRequestType?: string | null;
    /** 체크아웃 단계 생략 옵션(표시 보정·폴백용) */
    skipConfirmAndPreparing?: boolean;
    skipShippingAndDelivered?: boolean;
    statusDescription: string;
    totalAmount: number;
    discountAmount: number;
    finalAmount: number;
    userCouponId: number | null;
    shippingAddress: string;
    recipientName: string;
    recipientPhone: string;
    items: OrderItemResponse[];
    createdAt: string;
    updatedAt: string;
}

export interface OrderItemDetailResponse extends OrderItemResponse {
    productDescription: string;
    imageUrl: string;
}

export interface OrderDetailResponse extends OrderResponse {
    items: OrderItemDetailResponse[];
    /** REQUESTED | APPROVED | COMPLETED 등. 진행 중 취소가 없으면 undefined */
    activeCancelStatus?: string | null;
    payment?: {
        paymentId: number;
        paymentMethod: string;
        paymentDetails?: string;
        payAmount: number;
        status: string;
        paidAt: string;
    };
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

/**
 * 진행 중 취소·반품 건의 표시용 요청 유형. 서버가 `activeCancelRequestType`을 주면 우선하고,
 * 구버전 API에서는 취소 직전 상태가 배송 완료면 반품·환불로 간주한다.
 */
export function getEffectiveCancelRequestTypeForDisplay(order: {
    activeCancelRequestType?: string | null;
    statusBeforeCancelRequest?: string | null;
}): string | null {
    const raw = (order.activeCancelRequestType ?? '').trim();
    if (raw) return raw;
    if ((order.statusBeforeCancelRequest ?? '').toUpperCase() === 'DELIVERED') {
        return 'RETURN_REFUND';
    }
    return null;
}

/** API `progressStatus`를 우선하고, 없을 때만 결제 정보로 보조(구버전·캐시 대비). */
export function getUiProgressStatus(order: {
    status: string;
    progressStatus?: string | null;
    paymentStatus?: string | null;
    payment?: { status?: string };
    skipConfirmAndPreparing?: boolean;
    skipShippingAndDelivered?: boolean;
}): string {
    if (order.progressStatus) return order.progressStatus;
    const pay = (order.payment?.status || order.paymentStatus || '').toUpperCase();
    if (order.status === 'CANCEL_REQUESTED' && (pay === 'CANCELLED' || pay === 'REFUNDED')) {
        return 'CANCELLED';
    }
    const cancelled = order.status === 'CANCELLED' || order.status === 'CANCEL_REQUESTED';
    const paid = pay === 'COMPLETED';
    if (!cancelled && order.status === 'PENDING' && paid) {
        if (order.skipShippingAndDelivered && order.skipConfirmAndPreparing) return 'DELIVERED';
        if (order.skipConfirmAndPreparing) return 'SHIPPING';
        return 'CONFIRMED';
    }
    return order.status;
}

export const createOrder = async (data: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await api.post<{ data: OrderResponse }>('/api/orders', data);
    return response.data.data;
};

export const getMyOrders = async (page = 0, size = 20): Promise<PageResponse<OrderResponse>> => {
    const response = await api.get<{ data: PageResponse<OrderResponse> }>('/api/orders', { params: { page, size } });
    return response.data.data;
};

export const getOrderDetail = async (orderId: number): Promise<OrderDetailResponse> => {
    const response = await api.get<{ data: OrderDetailResponse }>(`/api/orders/${orderId}/detail`);
    return response.data.data;
};

export const cancelOrder = async (orderId: number): Promise<OrderResponse> => {
    const response = await api.put<{ data: OrderResponse }>(`/api/orders/${orderId}/cancel`);
    return response.data.data;
};
