import axios from 'axios';
import api from '../axios';

export interface CartItemResponse {
    cartItemId: number;
    productId: number;
    productName: string;
    productDescription: string;
    price: number;
    quantity: number;
    totalPrice: number;
    stockQuantity: number;
    imageUrl: string;
    isAvailable: boolean;
    createdAt: string;
}

export interface CartResponse {
    items: CartItemResponse[];
    totalItemCount: number;
    totalPrice: number;
}

export interface AddCartItemRequest {
    productId: number;
    quantity: number;
}

export interface UpdateCartItemRequest {
    quantity: number;
}

export const getCart = async (): Promise<CartResponse> => {
    const response = await api.get<{ data: CartResponse }>('/api/cart');
    return response.data.data;
};

export const addToCart = async (data: AddCartItemRequest): Promise<CartItemResponse> => {
    const response = await api.post<{ data: CartItemResponse }>('/api/cart', data);
    return response.data.data;
};

export const updateCartItem = async (productId: number, data: UpdateCartItemRequest): Promise<CartItemResponse> => {
    const response = await api.put<{ data: CartItemResponse }>(`/api/cart/${productId}`, data);
    return response.data.data;
};

export const removeFromCart = async (productId: number): Promise<void> => {
    await api.delete(`/api/cart/${productId}`);
};

export const clearCart = async (): Promise<void> => {
    await api.delete('/api/cart');
};

/** 주문 완료 후 주문에 포함된 상품만 장바구니에서 제거 (장바구니에 없으면 404 무시) */
export const removeOrderedProductsFromCart = async (productIds: number[]): Promise<void> => {
    const uniqueIds = [...new Set(productIds.filter((id) => Number.isFinite(id)))];
    await Promise.all(
        uniqueIds.map(async (productId) => {
            try {
                await removeFromCart(productId);
            } catch (err) {
                if (axios.isAxiosError(err) && err.response?.status === 404) {
                    return;
                }
                throw err;
            }
        })
    );
};

export const getCartItemCount = async (): Promise<number> => {
    const response = await api.get<{ data: number }>('/api/cart/count');
    return response.data.data;
};
