import { useState, useEffect } from 'react';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import Hero from './components/home/Hero';
import CategoryGrid from './components/home/CategoryGrid';
import ProductCard from './components/product/ProductCard';
import ProductListPage from './components/product/ProductListPage';
import ProductDetailPage from './components/product/ProductDetailPage';
import { ChevronLeft, ShoppingBag } from 'lucide-react';
import type { Product } from './types/product';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { fetchProducts } from './api/services/product';
import { getMyProfile } from './api/services/user';
import { getMyOrders } from './api/services/order';
import LoginModal from './components/auth/LoginModal';
import SignupModal from './components/auth/SignupModal';
import SignupPage from './components/auth/SignupPage';
import { useTranslation } from 'react-i18next';


function App() {
  const { t } = useTranslation();
  const [category, setCategory] = useState(t('common.category_all'));
  const [view, setView] = useState('home'); // 'home' | 'order' | 'benefit' | 'activity' | 'all_products' | 'product_detail' | 'signup'
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [timeLeft, setTimeLeft] = useState(24 * 60 * 60);
  const [isLoginOpen, setIsLoginOpen] = useState(false);
  const [isSignupOpen, setIsSignupOpen] = useState(false);

  const queryClient = useQueryClient();

  const { data: user } = useQuery({
    queryKey: ['user'],
    queryFn: getMyProfile,
    retry: false,
    refetchOnWindowFocus: false,
    enabled: !!sessionStorage.getItem('accessToken'),
    initialData: () => {
      const savedUser = sessionStorage.getItem('user');
      return savedUser ? JSON.parse(savedUser) : undefined;
    },
  });

  const { data: orders } = useQuery({
    queryKey: ['orders'],
    queryFn: getMyOrders,
    enabled: !!user,
  });

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft((prev) => (prev > 0 ? prev - 1 : 24 * 60 * 60));
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (seconds: number) => {
    const h = Math.floor(seconds / 3600).toString().padStart(2, '0');
    const m = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
    const s = (seconds % 60).toString().padStart(2, '0');
    return `${h}:${m}:${s}`;
  };

  const { data: productsPage, isLoading } = useQuery({
    queryKey: ['products', 'home'], // Changed key to avoid conflict with ProductListPage
    queryFn: () => fetchProducts(0, 4),
  });

  const products: Product[] = productsPage?.content || [];

  const categories = ['NEW ARRIVALS', 'WOMEN', 'MEN', 'HOME GOODS'];

  const handleProductClick = (product: Product) => {
    setSelectedProduct(product);
    setView('product_detail');
    window.scrollTo(0, 0);
  };

  const renderContent = () => {
    switch (view) {
      case 'home':
        return (
          <>
            <Hero onShopClick={() => setView('all_products')} />
            <CategoryGrid onCategoryClick={() => setView('all_products')} />
            <section className="max-w-7xl mx-auto px-6 py-20 border-t border-stone-200">
              <div className="flex justify-between items-end mb-10 text-left">
                <div>
                  <h2 className="text-2xl font-bold tracking-tight mb-2">DAILY DROPS</h2>
                  <p className="text-stone-500 text-sm">Special opportunities arriving every day at noon.</p>
                </div>
                <button
                  onClick={() => setView('all_products')}
                  className="text-sm font-medium border-b border-black pb-1 hover:text-stone-500 hover:border-stone-500 transition-colors"
                >
                  View All
                </button>
              </div>

              {isLoading ? (
                <div className="flex justify-center items-center h-40">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                  {products.slice(0, 3).map((product, index) => (
                    <div key={product.id} onClick={() => handleProductClick(product)} className="cursor-pointer">
                      <ProductCard
                        product={product}
                        timer={index === 0 ? formatTime(timeLeft) : undefined}
                      />
                    </div>
                  ))}
                </div>
              )}
            </section>
          </>
        );
      case 'all_products':
        // if (isLoading) return <div className="min-h-screen flex justify-center items-center"><div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div></div>;
        // ProductListPage now handles its own data fetching
        return <ProductListPage onProductClick={handleProductClick} />;
      case 'product_detail':
        return selectedProduct ? (
          <ProductDetailPage
            product={selectedProduct}
            onBack={() => setView('all_products')}
            onLoginRequest={() => setIsLoginOpen(true)}
          />
        ) : null;
      case 'order':
      case 'benefit':
      case 'activity':
        return (
          <div className="min-h-screen pt-12 pb-24 bg-[#f9f7f2]">
            <div className="max-w-3xl mx-auto px-6">
              <button
                onClick={() => setView('home')}
                className="flex items-center text-stone-400 hover:text-black transition-colors mb-12 group"
              >
                <ChevronLeft size={20} className="mr-2 transition-transform group-hover:-translate-x-1" />
                <span className="text-sm font-medium">{t('order.back_home')}</span>
              </button>

              <h2 className="text-4xl font-bold tracking-tight mb-12 text-stone-900 text-left">{getPageTitle()}</h2>

              <div className="bg-white p-10 rounded-[40px] shadow-sm border border-stone-100">
                {renderDetailContent()}
              </div>
            </div>
          </div>
        );
      case 'signup':
        return (
          <SignupPage
            onNavigate={setView}
            onLoginClick={() => {
              setView('home');
              setIsLoginOpen(true);
            }}
          />
        );
      default:
        return null;
    }
  };

  const renderDetailContent = () => {
    switch (view) {
      case 'order':
        if (!orders || orders.length === 0) {
          return (
            <div className="text-center py-20 text-stone-500">
              <p className="mb-4">{t('order.no_history')}</p>
              <button onClick={() => setView('all_products')} className="text-black underline font-bold">{t('order.go_shopping')}</button>
            </div>
          );
        }
        return (
          <div className="space-y-12 text-left">
            {orders.map((order) => (
              <div key={order.id} className="border-b border-stone-100 pb-12 last:border-0 last:pb-0">
                <div className="flex items-center justify-between mb-6">
                  <span className="text-xs font-bold text-stone-400 uppercase tracking-widest">{t('order.order_number')} {order.orderNumber}</span>
                  <span className="text-xs bg-blue-50 text-blue-600 px-3 py-1 rounded-full font-bold">{order.status}</span>
                </div>
                <div className="flex space-x-8 items-center">
                  <div className="w-32 h-32 bg-stone-100 rounded-3xl overflow-hidden flex items-center justify-center text-stone-300">
                    {/* Placeholder or fetch product image if available in order details */}
                    <ShoppingBag size={32} />
                  </div>
                  <div>
                    <h4 className="text-xl font-bold mb-2">{t('order.total')} {order.totalAmount.toLocaleString()}원</h4>
                    <p className="text-stone-400 text-sm mb-4">{t('order.items_etc', { id: order.id }).replace('Item', '상품').replace('etc...', '외...')}</p>
                  </div>
                </div>
                <div className="pt-8 flex justify-between items-center text-sm">
                  <span className="text-stone-500">{t('order.arrival_date', { date: '12월 14일' })}</span>
                  <button className="text-stone-900 font-bold border-b border-black">{t('order.shipping_detail')}</button>
                </div>
              </div>
            ))}
          </div>
        );
      case 'benefit':
        return (
          <div className="grid gap-6 text-left">
            <div className="p-8 rounded-3xl bg-stone-50 border border-stone-100">
              <p className="text-xs text-stone-400 font-bold uppercase mb-2">가용 쿠팡캐시</p>
              <div className="flex justify-between items-end">
                <h3 className="text-3xl font-bold">1,200원</h3>
                <button className="bg-black text-white px-6 py-2 rounded-full text-xs font-bold">충전하기</button>
              </div>
            </div>
            <div className="p-8 rounded-3xl border border-red-100 bg-red-50/30">
              <p className="text-xs text-red-400 font-bold uppercase mb-2">보유 쿠폰</p>
              <div className="flex justify-between items-end">
                <h3 className="text-3xl font-bold text-red-600">2장</h3>
                <p className="text-xs text-red-400 font-medium italic">이번 달 만료 쿠폰이 1장 있습니다.</p>
              </div>
            </div>
            <div className="pt-4">
              <h4 className="font-bold text-sm mb-4 uppercase text-stone-300 tracking-widest">사용 가능한 쿠폰 목록</h4>
              <div className="bg-white border border-stone-100 p-6 rounded-2xl flex justify-between items-center shadow-sm">
                <div>
                  <p className="font-bold">첫 구매 10% 할인 쿠폰</p>
                  <p className="text-xs text-stone-400 mt-1">~ 2024.12.31 까지 사용 가능</p>
                </div>
                <span className="text-red-500 font-bold">D-19</span>
              </div>
            </div>
          </div>
        );
      case 'activity':
        return (
          <div className="text-center py-12">
            <div className="mb-6 inline-flex w-16 h-16 bg-red-50 text-red-500 rounded-full items-center justify-center font-bold text-xl">1</div>
            <h3 className="text-xl font-bold mb-2">작성 가능한 리뷰가 있습니다.</h3>
            <p className="text-stone-400 text-sm mb-10">당신의 소중한 후기는 다른 분들께 큰 도움이 됩니다.</p>

            <div className="bg-stone-50 p-8 rounded-[30px] flex items-center space-x-6 text-left group cursor-pointer hover:bg-stone-100 transition-colors">
              <div className="w-20 h-20 bg-white rounded-2xl border border-stone-200 overflow-hidden">
                <img src="https://images.unsplash.com/photo-1614859324967-bdf219d45598?auto=format&fit=crop&q=80&w=800" className="w-full h-full object-cover" />
              </div>
              <div className="flex-1">
                <p className="text-xs text-stone-400 mb-1 font-bold">배송완료 (12.08)</p>
                <h4 className="font-bold group-hover:text-stone-600 transition-colors">Organic Ceramic Vase</h4>
                <button className="mt-4 bg-white px-5 py-2 rounded-full text-xs font-bold border border-stone-200">후기 작성하기</button>
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  const getPageTitle = () => {
    switch (view) {
      case 'order': return t('order.title');
      case 'benefit': return t('benefit.title');
      case 'activity': return t('activity.title');
      default: return '';
    }
  };

  return (
    <div className="antialiased">
      <Header
        category={category}
        setCategory={setCategory}
        categories={categories}
        onNavigate={setView}
        view={view}
        user={user}
        onLoginClick={() => setIsLoginOpen(true)}
        onLogoutClick={() => {
          sessionStorage.removeItem('accessToken');
          sessionStorage.removeItem('user');
          queryClient.setQueryData(['user'], null);
          window.location.reload(); // Simple logout
        }}
      />

      <LoginModal
        isOpen={isLoginOpen}
        onClose={() => setIsLoginOpen(false)}
        onLoginSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['user'] });
        }}
        onSignupClick={() => {
          setIsLoginOpen(false);
          setView('signup');
        }}

      />
      <SignupModal
        isOpen={isSignupOpen}
        onClose={() => setIsSignupOpen(false)}
        onSwitchToLogin={() => {
          setIsSignupOpen(false);
          setIsLoginOpen(true);
        }}
      />


      {renderContent()}

      <Footer />
    </div>
  );
}

export default App;
