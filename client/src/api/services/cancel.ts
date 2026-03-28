import api from '../axios';

export type CancelReason = 'CHANGE_OF_MIND' | 'WRONG_ORDER' | 'DUPLICATE_ORDER' | 'PRICE_CHANGE' | 'DELIVERY_DELAY' | 'OTHER';

/** 출고 전 취소 vs 배송 중/후 반품·환불 (API 미전달 시 서버에서 주문 취소로 처리) */
export type CancelRequestType = 'ORDER_CANCEL' | 'RETURN_REFUND';

export const CANCEL_REASON_LABELS: Record<CancelReason, string> = {
    CHANGE_OF_MIND: '단순 변심',
    WRONG_ORDER: '잘못된 주문',
    DUPLICATE_ORDER: '중복 주문',
    PRICE_CHANGE: '가격 변동',
    DELIVERY_DELAY: '배송 지연',
    OTHER: '기타',
};

export interface CancelItemRequest {
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
}

export interface CreateCancelRequest {
    orderId: number;
    orderNumber: string;
    cancelReason: CancelReason;
    cancelDetail?: string;
    requestType?: CancelRequestType;
    items: CancelItemRequest[];
}

export interface CancelItemResponse {
    id: number;
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
}

export interface CancelResponse {
    id: number;
    cancelNumber: string;
    orderId: number;
    orderNumber: string;
    userId: number;
    requestType?: CancelRequestType;
    requestTypeDescription?: string;
    cancelReason: string;
    cancelDetail: string | null;
    status: string;
    statusDescription: string;
    rejectedReason: string | null;
    items: CancelItemResponse[];
    createdAt: string;
    approvedAt: string | null;
    rejectedAt: string | null;
    completedAt: string | null;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export const createCancel = async (data: CreateCancelRequest): Promise<CancelResponse> => {
    const response = await api.post<{ data: CancelResponse }>('/api/cancels', data);
    return response.data.data;
};

export const getMyCancels = async (page = 0, size = 20): Promise<PageResponse<CancelResponse>> => {
    const response = await api.get<{ data: PageResponse<CancelResponse> }>('/api/cancels', { params: { page, size } });
    return response.data.data;
};

export const getCancel = async (cancelId: number): Promise<CancelResponse> => {
    const response = await api.get<{ data: CancelResponse }>(`/api/cancels/${cancelId}`);
    return response.data.data;
};
