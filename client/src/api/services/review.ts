import api from '../axios';

export interface ReviewResponse {
    id: number;
    userId: number;
    userName: string;
    productId: number;
    score: number;
    content: string;
    createdAt: string;
    imageUrl?: string;
}

export interface CreateReviewRequest {
    productId: number;
    score: number;
    content: string;
    imageUrl?: string;
}

export interface ReviewPageResponse {
    content: ReviewResponse[];
    totalPages: number;
    totalElements: number;
    last: boolean;
}

export const getProductReviews = async (productId: number, page = 0, size = 5): Promise<ReviewPageResponse> => {
    const response = await api.get<{ data: ReviewPageResponse }>(`/api/reviews/products/${productId}`, {
        params: { page, size, sort: 'createdAt,desc' }
    });
    return response.data.data;
};

export const createReview = async (data: CreateReviewRequest): Promise<ReviewResponse> => {
    const response = await api.post<{ data: ReviewResponse }>('/api/reviews', data);
    return response.data.data;
};

export const getMyReviews = async (page = 0, size = 10): Promise<ReviewPageResponse> => {
    const response = await api.get<{ data: ReviewPageResponse }>('/api/reviews/my', {
        params: { page, size, sort: 'createdAt,desc' }
    });
    return response.data.data;
};

export interface UpdateReviewRequest {
    score: number;
    content: string;
    imageUrl?: string;
}

export const updateReview = async (reviewId: number, data: UpdateReviewRequest): Promise<ReviewResponse> => {
    const response = await api.put<{ data: ReviewResponse }>(`/api/reviews/${reviewId}`, data);
    return response.data.data;
};

export const deleteReview = async (reviewId: number): Promise<void> => {
    await api.delete(`/api/reviews/${reviewId}`);
};
