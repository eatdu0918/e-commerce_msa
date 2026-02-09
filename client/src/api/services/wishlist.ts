import api from '../axios';

export interface WishlistItemResponse {
    wishlistItemId: number;
    productId: number;
    productName: string;
    productDescription: string;
    price: number;
    stockQuantity: number;
    imageUrl: string;
    isAvailable: boolean;
    createdAt: string;
}

export interface AddWishlistRequest {
    productId: number;
}

export const getWishlist = async (): Promise<WishlistItemResponse[]> => {
    const response = await api.get<{ data: WishlistItemResponse[] }>('/api/wishlist');
    return response.data.data;
};

export const addToWishlist = async (data: AddWishlistRequest): Promise<WishlistItemResponse> => {
    const response = await api.post<{ data: WishlistItemResponse }>('/api/wishlist', data);
    return response.data.data;
};

export const removeFromWishlist = async (productId: number): Promise<void> => {
    await api.delete(`/api/wishlist/${productId}`);
};

export const isInWishlist = async (productId: number): Promise<boolean> => {
    const response = await api.get<{ data: boolean }>(`/api/wishlist/check/${productId}`);
    return response.data.data;
};

export const getWishlistCount = async (): Promise<number> => {
    const response = await api.get<{ data: number }>('/api/wishlist/count');
    return response.data.data;
};

export const clearWishlist = async (): Promise<void> => {
    await api.delete('/api/wishlist');
};
