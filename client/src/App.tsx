import { Routes, Route, Outlet } from 'react-router-dom';
import RootLayout from './components/layout/RootLayout';
import AuthGuard from './components/auth/AuthGuard';
import { useTranslation } from 'react-i18next';
import { useQuery } from '@tanstack/react-query';
import { getMyProfile } from './api/services/user';
import { Suspense, lazy } from 'react';

// Lazy load components
const HomePage = lazy(() => import('./components/home/HomePage'));
const ProductListPage = lazy(() => import('./components/product/ProductListPage'));
const ProductDetailPage = lazy(() => import('./components/product/ProductDetailPage'));
const AdminLayout = lazy(() => import('./components/admin/AdminLayout'));
const AdminDashboard = lazy(() => import('./components/admin/AdminDashboard'));
const AdminProductList = lazy(() => import('./components/admin/AdminProductList'));
const AdminOrderList = lazy(() => import('./components/admin/AdminOrderList'));
const AdminUserList = lazy(() => import('./components/admin/AdminUserList'));
const AdminPaymentList = lazy(() => import('./components/admin/AdminPaymentList'));
const AdminCouponList = lazy(() => import('./components/admin/AdminCouponList'));
const AdminCancelList = lazy(() => import('./components/admin/AdminCancelList'));
const AdminRefundList = lazy(() => import('./components/admin/AdminRefundList'));

const SearchResultPage = lazy(() => import('./components/product/SearchResultPage'));
const NotFoundPage = lazy(() => import('./components/common/NotFoundPage'));
const SignupPage = lazy(() => import('./components/auth/SignupPage'));
const CheckoutPage = lazy(() => import('./components/checkout/CheckoutPage'));
const OrderCompletePage = lazy(() => import('./components/checkout/OrderCompletePage'));
const PaymentSuccessPage = lazy(() => import('./components/checkout/PaymentSuccessPage'));
const PaymentFailPage = lazy(() => import('./components/checkout/PaymentFailPage'));
const MyPageView = lazy(() => import('./components/profile/MyPageView'));
const EditProfileView = lazy(() => import('./components/profile/EditProfileView'));
const WishlistView = lazy(() => import('./components/profile/WishlistView'));
const OrderListView = lazy(() => import('./components/order/OrderListView'));
const OrderDetailView = lazy(() => import('./components/order/OrderDetailView'));
const CouponView = lazy(() => import('./components/profile/CouponView'));
const CancelRefundView = lazy(() => import('./components/profile/CancelRefundView'));
const PaymentHistoryView = lazy(() => import('./components/profile/PaymentHistoryView'));
const MyReviewsView = lazy(() => import('./components/profile/MyReviewsView'));
const ChangePasswordView = lazy(() => import('./components/profile/ChangePasswordView'));

import ScrollToTop from './components/common/ScrollToTop';

function App() {
  useTranslation();

  // Ensuring user data is loaded for protected routes
  useQuery({
    queryKey: ['user'],
    queryFn: getMyProfile,
    retry: false,
    refetchOnWindowFocus: false,
    enabled: !!sessionStorage.getItem('accessToken'),
  });

  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-black"></div></div>}>
      <ScrollToTop />
      <Routes>
        <Route element={<RootLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/shop" element={<ProductListPage />} />
          <Route path="/product/:id" element={<ProductDetailPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/order/complete/:id" element={<OrderCompletePage />} />
          <Route path="/orders/complete/:id" element={<OrderCompletePage />} />
          <Route path="/payment/success" element={<PaymentSuccessPage />} />
          <Route path="/payment/fail" element={<PaymentFailPage />} />
          <Route path="/search" element={<SearchResultPage />} />

          {/* User Routes - In real app, wrap in AuthGuard */}
          <Route element={<AuthGuard><Outlet /></AuthGuard>}>
            <Route path="/me" element={<MyPageView />} />
            <Route path="/me/profile" element={<EditProfileView />} />
            <Route path="/me/wishlist" element={<WishlistView />} />
            <Route path="/me/orders" element={<OrderListView />} />
            <Route path="/me/orders/:id" element={<OrderDetailView />} />
            <Route path="/me/coupons" element={<CouponView />} />
            <Route path="/me/cancel-refund" element={<CancelRefundView />} />
            <Route path="/me/payments" element={<PaymentHistoryView />} />
            <Route path="/me/reviews" element={<MyReviewsView />} />
            <Route path="/me/password" element={<ChangePasswordView />} />
          </Route>

        </Route>

        <Route path="/signup" element={<SignupPage />} />

        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboard />} />
          <Route path="users" element={<AdminUserList />} />
          <Route path="products" element={<AdminProductList />} />
          <Route path="orders" element={<AdminOrderList />} />
          <Route path="payments" element={<AdminPaymentList />} />
          <Route path="coupons" element={<AdminCouponList />} />
          <Route path="cancels" element={<AdminCancelList />} />
          <Route path="refunds" element={<AdminRefundList />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}

export default App;
