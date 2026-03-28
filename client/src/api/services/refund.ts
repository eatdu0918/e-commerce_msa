import api from '../axios';

/** refund-service RefundResponse 와 필드명 일치 (금액은 amount) */
export interface RefundResponse {
    id: number;
    cancelId: number;
    orderId: number;
    userId: number;
    paymentId?: number | null;
    refundNumber?: string | null;
    /** 서버 BigDecimal → JSON 숫자 또는 문자열 */
    amount?: number | string | null;
    status: string;
    statusDescription?: string;
    refundReason?: string;
    refundReasonDescription?: string;
    refundDetail?: string | null;
    refundMethod?: string | null;
    completedAt?: string | null;
    failedAt?: string | null;
    failureReason?: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export const getMyRefunds = async (page = 0, size = 20): Promise<PageResponse<RefundResponse>> => {
    const response = await api.get<{ data: PageResponse<RefundResponse> }>('/api/refunds', { params: { page, size } });
    return response.data.data;
};

export const getRefund = async (refundId: number): Promise<RefundResponse> => {
    const response = await api.get<{ data: RefundResponse }>(`/api/refunds/${refundId}`);
    return response.data.data;
};

export const getRefundByCancelId = async (cancelId: number): Promise<RefundResponse> => {
    const response = await api.get<{ data: RefundResponse }>(`/api/refunds/cancel/${cancelId}`);
    return response.data.data;
};
