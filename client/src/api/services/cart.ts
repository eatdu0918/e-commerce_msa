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

export const getCartItemCount = async (): Promise<number> => {
    const response = await api.get<{ data: number }>('/api/cart/count');
    return response.data.data;
};
