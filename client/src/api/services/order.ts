import api from '../axios';

export interface CreateOrderRequest {
    userId: number;
    productId: number;
    quantity: number;
    shippingAddress: string;
}

export interface OrderItemResponse {
    id: number;
    productId: number;
    productName: string;
    price: number;
    quantity: number;
    totalPrice: number;
}

export interface OrderResponse {
    id: number;
    userId: number;
    orderNumber: string;
    status: string;
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
    payment?: {
        paymentId: number;
        paymentMethod: string;
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
