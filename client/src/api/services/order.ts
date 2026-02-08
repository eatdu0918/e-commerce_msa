import api from '../axios';

export interface CreateOrderRequest {
    userId: number;
    productId: number;
    quantity: number;
    shippingAddress: string;
}

export interface OrderResponse {
    id: number;
    orderNumber: string;
    status: string;
    totalAmount: number;
    // ...
}

export const createOrder = async (data: CreateOrderRequest): Promise<OrderResponse> => {
    const response = await api.post<{ data: OrderResponse }>('/api/orders', data);
    return response.data.data;
};

export const getMyOrders = async (): Promise<OrderResponse[]> => {
    const response = await api.get<{ data: OrderResponse[] }>('/api/orders/my');
    return response.data.data;
};
