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
  totalAmount: number;
  status: string;
  /** 결제·취소 반영 후 목록 표시용(있으면 우선) */
  progressStatus?: string | null;
  recipientName: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderDetail {
  orderId: number;
  userId: number;
  userEmail: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  items: OrderItem[];
  payment?: Payment;
}

export interface OrderItem {
  orderItemId: number;
  productId: number;
  productName: string;
  productPrice: number;
  quantity: number;
  itemPrice: number;
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

export interface Coupon {
  id: number;
  code: string;
  name: string;
  discountType: string;
  discountValue: number;
  minimumPurchaseAmount: number;
  maxDiscountAmount: number;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
  createdAt: string;
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
  discountType: string;
  discountValue: number;
  minimumPurchaseAmount: number;
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
    const response = await api.post<ApiResponse<Coupon>>('/api/admin/coupons', data);
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

  // Cancels
  getCancels: async (page = 0, size = 10, status?: string) => {
    const statusParam = status ? `&status=${status}` : '';
    const response = await api.get<ApiResponse<PageResponse<Cancel>>>(
      `/api/admin/cancels?page=${page}&size=${size}${statusParam}`
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
