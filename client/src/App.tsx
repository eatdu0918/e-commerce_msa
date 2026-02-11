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
const SignupPage = lazy(() => import('./components/auth/SignupPage'));
const CheckoutPage = lazy(() => import('./components/checkout/CheckoutPage'));
const OrderCompletePage = lazy(() => import('./components/checkout/OrderCompletePage'));
const MyPageView = lazy(() => import('./components/profile/MyPageView'));
const EditProfileView = lazy(() => import('./components/profile/EditProfileView'));
const WishlistView = lazy(() => import('./components/profile/WishlistView'));
const OrderListView = lazy(() => import('./components/order/OrderListView'));
const OrderDetailView = lazy(() => import('./components/order/OrderDetailView'));
const CouponView = lazy(() => import('./components/profile/CouponView'));
const CancelRefundView = lazy(() => import('./components/profile/CancelRefundView'));
const PaymentHistoryView = lazy(() => import('./components/profile/PaymentHistoryView'));

function App() {
  const { i18n } = useTranslation();

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
      <Routes>
        <Route element={<RootLayout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/shop" element={<ProductListPage />} />
          <Route path="/product/:id" element={<ProductDetailPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/order/complete/:id" element={<OrderCompletePage />} />

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
          </Route>

        </Route>

        <Route path="/signup" element={<SignupPage />} />
      </Routes>
    </Suspense>
  );
}

export default App;
