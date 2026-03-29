import api from '../axios';

// ============================================================
// Types
// ============================================================

export interface User {
  id: number;
  email: string;
  name: string;
  role: string;
  createdAt: string;
  isActive: boolean;
}

export interface Order {
  id: number;
  userId: number;
  orderNumber?: string;
  totalAmount: number;
  discountAmount?: number;
  finalAmount?: number;
  appliedCouponName?: string | null;
  appliedCouponCode?: string | null;
  appliedCouponType?: string | null;
  appliedCouponRuleValue?: number | null;
  status: string;
  /** 결제·취소 반영 후 목록 표시용(있으면 우선) */
  progressStatus?: string | null;
  /** 집계 조회 시 결제 상태 */
  paymentStatus?: string | null;
  /** 진행 중 취소(REQUESTED/APPROVED/COMPLETED) */
  activeCancelStatus?: string | null;
  /** 진행 중 취소 건 ID(관리자 승인·거절) */
  activeCancelId?: number | null;
  /** ORDER_CANCEL | RETURN_REFUND */
  activeCancelRequestType?: string | null;
  statusBeforeCancelRequest?: string | null;
  recipientName: string;
  createdAt: string;
  updatedAt: string;
  skipConfirmAndPreparing?: boolean;
  skipShippingAndDelivered?: boolean;
}

/** GET /api/admin/orders/{id}/detail 응답 */
export interface OrderDetail {
  id: number;
  orderId?: number;
  userId: number;
  orderNumber?: string;
  status: string;
  statusDescription?: string;
  totalAmount: number;
  discountAmount?: number;
  finalAmount?: number;
  appliedCouponName?: string | null;
  appliedCouponCode?: string | null;
  appliedCouponType?: string | null;
  appliedCouponRuleValue?: number | null;
  shippingAddress?: string;
  recipientName?: string;
  recipientPhone?: string;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
  payment?: AdminOrderPaymentInfo | null;
  progressStatus?: string | null;
  activeCancelStatus?: string | null;
  activeCancelId?: number | null;
  /** ORDER_CANCEL | RETURN_REFUND */
  activeCancelRequestType?: string | null;
  /** 취소 요청 직전 주문 상태(예: SHIPPING 차단 판별) */
  statusBeforeCancelRequest?: string | null;
  skipConfirmAndPreparing?: boolean;
  skipShippingAndDelivered?: boolean;
}

export interface AdminOrderPaymentInfo {
  id: number;
  orderId: number;
  paymentNumber?: string;
  status: string;
  amount: number;
  paymentMethod: string;
  paymentDetails?: string;
  paidAt?: string;
}

export interface OrderItem {
  id: number;
  orderItemId?: number;
  productId: number;
  productName: string;
  productPrice?: number;
  unitPrice?: number;
  quantity: number;
  itemPrice?: number;
  totalPrice: number;
  imageUrl?: string | null;
  categoryName?: string | null;
  currentStock?: number | null;
}

export interface Payment {
  id: number;
  orderId: number;
  amount: number;
  status: string;
  paymentMethod: string;
  createdAt: string;
  updatedAt: string;
}

/** GET /api/admin/coupons — discount-service CouponResponse */
export interface Coupon {
  id: number;
  code: string;
  name: string;
  description?: string;
  couponType: string;
  discountValue: number | string;
  minOrderAmount?: number | string | null;
  maxDiscountAmount?: number | string | null;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
  createdAt: string;
  /** 구 클라이언트/목 필드 호환 */
  discountType?: string;
  minimumPurchaseAmount?: number | string | null;
}

export interface CancelItemRow {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number | string;
  totalPrice?: number | string;
}

export interface Cancel {
  id: number;
  orderId: number;
  orderNumber?: string;
  cancelNumber?: string;
  userId: number;
  requestType?: string;
  requestTypeDescription?: string;
  cancelReason: string;
  cancelReasonDescription?: string;
  cancelDetail?: string;
  status: string;
  statusDescription?: string;
  createdAt: string;
  updatedAt: string;
  rejectedReason?: string;
  items?: CancelItemRow[];
  approvedAt?: string | null;
  rejectedAt?: string | null;
  completedAt?: string | null;
}

export interface Refund {
  id: number;
  cancelId: number;
  orderId: number;
  amount: number;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: {
    errorCode: string;
    errorMessage: string;
  };
  timestamp: string;
}

export interface CreateCouponRequest {
  code: string;
  name: string;
  discountType: string;
  discountValue: number;
  minimumPurchaseAmount: number;
  maxDiscountAmount: number;
  validFrom: string;
  validUntil: string;
}

export interface UpdateCouponRequest {
  name: string;
  description?: string;
  couponType: string;
  discountValue: number;
  minOrderAmount: number;
  maxDiscountAmount: number;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: number;
  categoryName: string;
  imageUrl: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: number;
  imageUrl?: string;
}

export interface UpdateProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: number;
  imageUrl?: string;
}

// ============================================================
// API Services
// ============================================================

export const adminApi = {
  // Products
  getProducts: async (page = 0, size = 10, keyword?: string) => {
    const keywordParam = keyword ? `&keyword=${encodeURIComponent(keyword)}` : '';
    const response = await api.get<ApiResponse<PageResponse<Product>>>(
      `/api/products?page=${page}&size=${size}${keywordParam}`
    );
    return response.data;
  },

  getProduct: async (id: number) => {
    const response = await api.get<ApiResponse<Product>>(`/api/products/${id}`);
    return response.data;
  },

  createProduct: async (data: CreateProductRequest) => {
    const response = await api.post<ApiResponse<Product>>('/api/products', data);
    return response.data;
  },

  updateProduct: async (id: number, data: UpdateProductRequest) => {
    const response = await api.put<ApiResponse<Product>>(`/api/products/${id}`, data);
    return response.data;
  },

  deleteProduct: async (id: number) => {
    const response = await api.delete<ApiResponse<void>>(`/api/products/${id}`);
    return response.data;
  },

  // Users
  getUsers: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<PageResponse<User>>>(
      `/api/admin/users?page=${page}&size=${size}`
    );
    return response.data;
  },

  getUser: async (id: number) => {
    const response = await api.get<ApiResponse<User>>(`/api/admin/users/${id}`);
    return response.data;
  },

  deleteUser: async (id: number) => {
    const response = await api.delete<ApiResponse<void>>(`/api/admin/users/${id}`);
    return response.data;
  },

  updateUserRole: async (id: number, role: string) => {
    const response = await api.put<ApiResponse<User>>(
      `/api/admin/users/${id}/role?role=${role}`
    );
    return response.data;
  },

  // Orders
  getOrders: async (page = 0, size = 10, status?: string) => {
    const statusParam = status ? `&status=${status}` : '';
    const response = await api.get<ApiResponse<PageResponse<Order>>>(
      `/api/admin/orders?page=${page}&size=${size}${statusParam}`
    );
    return response.data;
  },

  getOrder: async (id: number) => {
    const response = await api.get<ApiResponse<Order>>(`/api/admin/orders/${id}`);
    return response.data;
  },

  getOrderDetail: async (id: number) => {
    const response = await api.get<ApiResponse<OrderDetail>>(
      `/api/admin/orders/${id}/detail`
    );
    return response.data;
  },

  updateOrderStatus: async (id: number, status: string) => {
    const response = await api.put<ApiResponse<Order>>(
      `/api/admin/orders/${id}/status`,
      { status }
    );
    return response.data;
  },

  // Payments
  getPayments: async (page = 0, size = 10, status?: string) => {
    const statusParam = status ? `&status=${status}` : '';
    const response = await api.get<ApiResponse<PageResponse<Payment>>>(
      `/api/admin/payments?page=${page}&size=${size}${statusParam}`
    );
    return response.data;
  },

  getPayment: async (id: number) => {
    const response = await api.get<ApiResponse<Payment>>(`/api/admin/payments/${id}`);
    return response.data;
  },

  updatePaymentStatus: async (id: number, status: string) => {
    const response = await api.put<ApiResponse<Payment>>(
      `/api/admin/payments/${id}/status`,
      { status }
    );
    return response.data;
  },

  // Coupons
  createCoupon: async (data: CreateCouponRequest) => {
    const couponType =
      data.discountType === 'PERCENTAGE'
        ? 'PERCENTAGE'
        : data.discountType === 'FIXED_AMOUNT' || data.discountType === 'FIXED'
          ? 'FIXED_AMOUNT'
          : data.discountType;
    const toLocalDateTime = (s: string) => (s.length === 16 ? `${s}:00` : s);
    const payload = {
      code: data.code,
      name: data.name,
      couponType,
      discountValue: data.discountValue,
      minOrderAmount: data.minimumPurchaseAmount,
      maxDiscountAmount: data.maxDiscountAmount,
      validFrom: toLocalDateTime(data.validFrom),
      validUntil: toLocalDateTime(data.validUntil),
    };
    const response = await api.post<ApiResponse<Coupon>>('/api/admin/coupons', payload);
    return response.data;
  },

  getCoupons: async (page = 0, size = 10) => {
    const response = await api.get<ApiResponse<PageResponse<Coupon>>>(
      `/api/admin/coupons?page=${page}&size=${size}`
    );
    return response.data;
  },

  getCoupon: async (id: number) => {
    const response = await api.get<ApiResponse<Coupon>>(`/api/admin/coupons/${id}`);
    return response.data;
  },

  updateCoupon: async (id: number, data: UpdateCouponRequest) => {
    const response = await api.put<ApiResponse<Coupon>>(
      `/api/admin/coupons/${id}`,
      data
    );
    return response.data;
  },

  deleteCoupon: async (id: number) => {
    const response = await api.delete<ApiResponse<void>>(`/api/admin/coupons/${id}`);
    return response.data;
  },

  // Cancels / Returns (동일 API, requestType으로 구분)
  getCancels: async (
    page = 0,
    size = 10,
    status?: string,
    requestType?: 'ORDER_CANCEL' | 'RETURN_REFUND'
  ) => {
    const statusParam = status ? `&status=${encodeURIComponent(status)}` : '';
    const typeParam = requestType
      ? `&requestType=${encodeURIComponent(requestType)}`
      : '';
    const response = await api.get<ApiResponse<PageResponse<Cancel>>>(
      `/api/admin/cancels?page=${page}&size=${size}${statusParam}${typeParam}`
    );
    return response.data;
  },

  getCancel: async (id: number) => {
    const response = await api.get<ApiResponse<Cancel>>(`/api/admin/cancels/${id}`);
    return response.data;
  },

  approveCancel: async (id: number) => {
    const response = await api.put<ApiResponse<Cancel>>(
      `/api/admin/cancels/${id}/approve`
    );
    return response.data;
  },

  rejectCancel: async (id: number, rejectedReason: string) => {
    const response = await api.put<ApiResponse<Cancel>>(
      `/api/admin/cancels/${id}/reject`,
      { rejectedReason }
    );
    return response.data;
  },

  // Refunds
  getRefunds: async (page = 0, size = 10, status?: string) => {
    const statusParam = status ? `&status=${status}` : '';
    const response = await api.get<ApiResponse<PageResponse<Refund>>>(
      `/api/admin/refunds?page=${page}&size=${size}${statusParam}`
    );
    return response.data;
  },

  getRefund: async (id: number) => {
    const response = await api.get<ApiResponse<Refund>>(`/api/admin/refunds/${id}`);
    return response.data;
  },

  retryRefund: async (id: number) => {
    const response = await api.put<ApiResponse<Refund>>(
      `/api/admin/refunds/${id}/retry`
    );
    return response.data;
  },
};
