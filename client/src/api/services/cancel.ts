import api from '../axios';

export interface CancelResponse {
    id: number;
    orderId: number;
    userId: number;
    cancelReason: string;
    status: string;
    statusDescription: string;
    rejectedReason: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreateCancelRequest {
    orderId: number;
    cancelReason: string;
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
