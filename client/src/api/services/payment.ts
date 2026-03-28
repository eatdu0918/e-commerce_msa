import api from '../axios';

export interface PaymentResponse {
    id: number;
    orderId: number;
    userId: number;
    paymentMethod: string;
    paymentDetails?: string;
    payAmount: number;
    status: string;
    statusDescription: string;
    paidAt: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreatePaymentRequest {
    orderId: number;
    orderNumber: string;
    paymentMethod: string;
    amount: number;
    paymentDetails?: string;
}

export interface PageResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export const createPayment = async (data: CreatePaymentRequest): Promise<PaymentResponse> => {
    const response = await api.post<{ data: PaymentResponse }>('/api/payments', data);
    return response.data.data;
};

export const getMyPayments = async (page = 0, size = 20): Promise<PageResponse<PaymentResponse>> => {
    const response = await api.get<{ data: PageResponse<PaymentResponse> }>('/api/payments', { params: { page, size } });
    return response.data.data;
};

export const getPayment = async (paymentId: number): Promise<PaymentResponse> => {
    const response = await api.get<{ data: PaymentResponse }>(`/api/payments/${paymentId}`);
    return response.data.data;
};

export const getPaymentByOrderId = async (orderId: number): Promise<PaymentResponse> => {
    const response = await api.get<{ data: PaymentResponse }>(`/api/payments/order/${orderId}`);
    return response.data.data;
};
