import api from '../axios';

export interface UserCouponResponse {
    id: number;
    userId: number;
    couponId: number;
    couponCode: string;
    couponName: string;
    couponDescription: string;
    couponType: string;
    discountValue: number;
    minOrderAmount: number;
    maxDiscountAmount: number;
    status: string;
    usedAt: string | null;
    orderId: number | null;
    validFrom: string;
    validUntil: string;
    isAvailable: boolean;
    createdAt: string;
}

export const getMyCoupons = async (): Promise<UserCouponResponse[]> => {
    const response = await api.get<{ data: UserCouponResponse[] }>('/api/coupons');
    return response.data.data;
};

export const getAvailableCoupons = async (): Promise<UserCouponResponse[]> => {
    const response = await api.get<{ data: UserCouponResponse[] }>('/api/coupons/available');
    return response.data.data;
};

export const claimCoupon = async (code: string): Promise<UserCouponResponse> => {
    const response = await api.post<{ data: UserCouponResponse }>(`/api/coupons/claim/${code}`);
    return response.data.data;
};

export interface CalculateDiscountRequest {
    userCouponId: number;
    totalAmount: number;
}

export interface DiscountCalculationResponse {
    originalAmount: number;
    discountAmount: number;
    finalAmount: number;
    couponName: string;
    couponType: string;
}

export const calculateDiscount = async (data: CalculateDiscountRequest): Promise<DiscountCalculationResponse> => {
    const response = await api.post<{ data: DiscountCalculationResponse }>('/api/coupons/calculate', data);
    return response.data.data;
};
