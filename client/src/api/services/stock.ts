import api from '../axios';

export interface StockResponse {
    productId: number;
    quantity: number;
    updatedAt: string;
}

export interface StockCheckResponse {
    productId: number;
    requestedQuantity: number;
    availableQuantity: number;
    available: boolean;
}

export const getStock = async (productId: number): Promise<StockResponse> => {
    const response = await api.get<{ data: StockResponse }>(`/api/stocks/${productId}`);
    return response.data.data;
};

export const checkStock = async (productId: number, quantity: number): Promise<StockCheckResponse> => {
    const response = await api.get<{ data: StockCheckResponse }>('/api/stocks/check', {
        params: { productId, quantity }
    });
    return response.data.data;
};
