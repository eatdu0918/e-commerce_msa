import api from '../axios';

export interface RefundResponse {
    id: number;
    cancelId: number;
    orderId: number;
    userId: number;
    refundAmount: number;
    status: string;
    statusDescription: string;
    refundMethod: string;
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
