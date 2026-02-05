import { useState, useEffect } from 'react';
import Header from './components/layout/Header';
import Footer from './components/layout/Footer';
import Hero from './components/home/Hero';
import CategoryGrid from './components/home/CategoryGrid';
import ProductCard from './components/product/ProductCard';
import ProductListPage from './components/product/ProductListPage';
import ProductDetailPage from './components/product/ProductDetailPage';
import { ChevronLeft } from 'lucide-react';
import type { Product } from './types/product';

function App() {
  const [category, setCategory] = useState('전체');
  const [view, setView] = useState('home'); // 'home' | 'order' | 'benefit' | 'activity' | 'all_products' | 'product_detail'
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [timeLeft, setTimeLeft] = useState(24 * 60 * 60);

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

  const products: Product[] = [
    {
      id: 1,
      name: "Organic Ceramic Vase",
      category: "Home Goods",
      price: 54000,
      originalPrice: 89000,
      date: "2024-12-10",
      description: "공간의 분위기를 바꿔주는 핸드메이드 화병.",
      image: "/assets/images/product_vase.png",
      rating: 4.8,
      reviews: 342,
      badge: "Home Goods",
      discount: 39
    },
    {
      id: 2,
      name: "Oversized Sand Hoodie",
      category: "Apparel",
      price: 72000,
      date: "2024-12-08",
      description: "편안한 핏의 프리미엄 코튼 후디.",
      image: "/assets/images/product_hoodie.png",
      rating: 4.9,
      reviews: 521,
      badge: "Apparel"
    },
    {
      id: 3,
      name: "Urban Tech Runner v.1",
      category: "Footwear",
      price: 129000,
      date: "2024-12-05",
      description: "기능성과 스타일을 동시에 잡은 러닝슈즈.",
      image: "/assets/images/product_runner.png",
      rating: 5.0,
      reviews: 128,
      badge: "Footwear"
    },
    {
      id: 4,
      name: "Minimalist Linen Shirt",
      category: "Apparel",
      price: 89000,
      date: "2024-11-28",
      description: "여름철 필수적인 린넨 소재의 베이직 셔츠.",
      image: "/product1.png",
      rating: 4.7,
      reviews: 215,
      badge: "Apparel"
    },
    {
      id: 5,
      name: "Stone Coaster Set",
      category: "Home Goods",
      price: 24000,
      date: "2024-11-20",
      description: "천연석으로 제작된 고급스러운 코스터 세트.",
      image: "/product2.png",
      rating: 4.6,
      reviews: 89,
      badge: "Home Goods"
    },
    {
      id: 6,
      name: "Classic Cotton Tote",
      category: "Home Goods",
      price: 15000,
      date: "2024-11-15",
      description: "가볍고 튼튼한 캔버스 토트백.",
      image: "/product3.png",
      rating: 4.5,
      reviews: 456,
      badge: "Home Goods"
    },
    {
      id: 7,
      name: "Urban Minimalist Tee",
      category: "Apparel",
      price: 39000,
      date: "2024-11-10",
      description: "미니멀한 디자인의 프리미엄 코튼 티셔츠.",
      image: "/product4.png",
      rating: 4.4,
      reviews: 128,
      badge: "Apparel"
    },
    {
      id: 8,
      name: "Premium Leather Jacket",
      category: "Apparel",
      price: 299000,
      date: "2024-12-12",
      description: "시간이 흐를수록 멋을 더하는 천연 가죽 자켓.",
      image: "/product_leather_jacket_1770129969469.png",
      rating: 4.9,
      reviews: 87,
      badge: "NEW"
    },
    {
      id: 9,
      name: "White Urban Sneakers",
      category: "Footwear",
      price: 158000,
      date: "2024-12-11",
      description: "어느 차림에도 잘 어울리는 깨끗한 화이트 스니커즈.",
      image: "/product_white_sneakers_1770129984418.png",
      rating: 4.8,
      reviews: 215,
      badge: "Best"
    },
    {
      id: 10,
      name: "Modern Leather Bag",
      category: "Home Goods",
      price: 185000,
      date: "2024-11-12",
      description: "심플한 라인이 돋보이는 데일리 레더 백.",
      image: "/product_leather_bag_1770129999630.png",
      rating: 4.8,
      reviews: 152,
      badge: "Home Goods"
    },
    {
      id: 11,
      name: "Minimalist Watch",
      category: "Home Goods",
      price: 210000,
      date: "2024-11-09",
      description: "불필요한 요소를 걷어낸 세련된 디자인의 시계.",
      image: "/product_watch_1770130025997.png",
      rating: 4.7,
      reviews: 64,
      badge: "Home Goods"
    },
    {
      id: 12,
      name: "Urban Sunglasses",
      category: "Home Goods",
      price: 145000,
      date: "2024-11-07",
      description: "어떤 얼굴형에도 잘 어울리는 모던한 선글라스.",
      image: "/product_sunglasses_1770130041859.png",
      rating: 4.6,
      reviews: 93,
      badge: "Home Goods"
    },
    {
      id: 13,
      name: "Cashmere Scarf",
      category: "Apparel",
      price: 95000,
      date: "2024-11-03",
      description: "부드러운 캐시미어 혼방 소재의 웜 스카프.",
      image: "/product_scarf_1770130055870.png",
      rating: 4.8,
      reviews: 218,
      badge: "Apparel"
    },
    {
      id: 14,
      name: "Signature Hat",
      category: "Apparel",
      price: 45000,
      date: "2024-11-01",
      description: "어반 스레드만의 미니멀한 감성이 담긴 캡.",
      image: "/product_hat_1770130071329.png",
      rating: 4.5,
      reviews: 312,
      badge: "Apparel"
    },
    {
      id: 15,
      name: "Minimalist Dress",
      category: "Apparel",
      price: 125000,
      date: "2024-11-25",
      description: "우아하고 심플한 실루엣의 드레스.",
      image: "/product_dress_1770130098881.png",
      rating: 4.7,
      reviews: 84,
      badge: "Apparel"
    },
    {
      id: 16,
      name: "Classic Boots",
      category: "Footwear",
      price: 245000,
      date: "2024-11-22",
      description: "내구성과 스타일을 겸비한 클래식 부츠.",
      image: "/product_boots_1770130114393.png",
      rating: 4.9,
      reviews: 42,
      badge: "Footwear"
    },
    {
      id: 17,
      name: "Minimalist Earrings",
      category: "Home Goods",
      price: 68000,
      date: "2024-11-18",
      description: "은은한 포인트가 되어주는 미니멀 아이템.",
      image: "/product_earrings_1770130143031.png",
      rating: 4.8,
      reviews: 124,
      badge: "Home Goods"
    },
    {
      id: 18,
      name: "Tech Urban Backpack",
      category: "Home Goods",
      price: 165000,
      date: "2024-11-15",
      description: "수납력과 디자인을 모두 고려한 어반 백팩.",
      image: "/product_backpack_1770130171952.png",
      rating: 4.7,
      reviews: 95,
      badge: "Home Goods"
    },
    {
      id: 19,
      name: "Premium Slim Wallet",
      category: "Home Goods",
      price: 59000,
      date: "2024-12-15",
      description: "최고급 가죽으로 제작된 슬림한 머니클립 지갑.",
      image: "/assets/images/product_wallet.png",
      rating: 4.9,
      reviews: 67,
      badge: "NEW"
    },
    {
      id: 20,
      name: "Modern Desk Lamp",
      category: "Home Goods",
      price: 125000,
      date: "2024-12-14",
      description: "건축학적 디자인의 세련된 데스크 램프.",
      image: "/assets/images/product_lamp.png",
      rating: 4.8,
      reviews: 42,
      badge: "Home Goods"
    },
    {
      id: 21,
      name: "Scented Soy Candle",
      category: "Home Goods",
      price: 32000,
      date: "2024-12-13",
      description: "자연의 향을 담은 프리미엄 소이 캔들.",
      image: "/assets/images/product_candle.png",
      rating: 4.7,
      reviews: 128,
      badge: "Best"
    },
    {
      id: 22,
      name: "Felt Laptop Sleeve",
      category: "Home Goods",
      price: 45000,
      date: "2024-12-12",
      description: "소중한 기기를 보호하는 미니멀한 펠트 슬리브.",
      image: "/assets/images/product_laptop_sleeve.png",
      rating: 4.6,
      reviews: 84,
      badge: "Home Goods"
    },
    {
      id: 23,
      name: "Wool Blend Cardigan",
      category: "Apparel",
      price: 158000,
      date: "2024-12-11",
      description: "포근한 감촉의 프리미엄 가디건.",
      image: "/assets/images/product_cardigan.png",
      rating: 4.9,
      reviews: 52,
      badge: "Apparel"
    },
    {
      id: 24,
      name: "Designer Table Book",
      category: "Home Goods",
      price: 89000,
      date: "2024-12-10",
      description: "공간을 완성하는 디자이너 아트 북.",
      image: "/assets/images/product_book.png",
      rating: 4.8,
      reviews: 31,
      badge: "Home Goods"
    },
    {
      id: 25,
      name: "Ceramic Mug Set",
      category: "Home Goods",
      price: 42000,
      date: "2024-12-09",
      description: "핸드메이드 감성의 세라믹 머그 세트.",
      image: "/assets/images/product_mug.png",
      rating: 4.7,
      reviews: 156,
      badge: "Home Goods"
    },
    {
      id: 26,
      name: "Premium Headphones",
      category: "Home Goods",
      price: 399000,
      date: "2024-12-08",
      description: "최고의 음질과 디자인을 겸비한 헤드폰.",
      image: "/assets/images/product_headphones.png",
      rating: 5.0,
      reviews: 24,
      badge: "NEW"
    },
    {
      id: 27,
      name: "Round Wooden Mirror",
      category: "Home Goods",
      price: 185000,
      date: "2024-12-07",
      description: "공간을 넓어 보이게 하는 내추럴 우드 거울.",
      image: "/assets/images/product_mirror.png",
      rating: 4.8,
      reviews: 12,
      badge: "Home Goods"
    },
    {
      id: 28,
      name: "Designer Wooden Chair",
      category: "Home Goods",
      price: 420000,
      date: "2024-12-06",
      description: "장인 정신이 깃든 우아한 실루엣의 의자.",
      image: "/assets/images/product_chair.png",
      rating: 4.9,
      reviews: 8,
      badge: "Home Goods"
    },
    {
      id: 29,
      name: "Leather Bound Notebook",
      category: "Home Goods",
      price: 48000,
      date: "2024-12-05",
      description: "기록의 가치를 더하는 프리미엄 가죽 노트.",
      image: "/assets/images/product_notebook.png",
      rating: 4.7,
      reviews: 45,
      badge: "NEW"
    },
    {
      id: 30,
      name: "Matte Ceramic Plate Set",
      category: "Home Goods",
      price: 76000,
      date: "2024-12-04",
      description: "테이블에 품격을 더하는 무광 세라믹 식기.",
      image: "/assets/images/product_plate_set.png",
      rating: 4.6,
      reviews: 28,
      badge: "Home Goods"
    },
    {
      id: 31,
      name: "Walnut Cutting Board",
      category: "Home Goods",
      price: 115000,
      date: "2024-12-03",
      description: "주방의 품격을 높이는 월넛 소재 통원목 도마.",
      image: "/assets/images/product_cutting_board.png",
      rating: 4.9,
      reviews: 15,
      badge: "Best"
    },
    {
      id: 32,
      name: "Metal Desk Organizer",
      category: "Home Goods",
      price: 39000,
      date: "2024-12-02",
      description: "책상을 정돈해주는 세련된 메탈 오거나이저.",
      image: "/assets/images/product_organizer.png",
      rating: 4.5,
      reviews: 56,
      badge: "Home Goods"
    },
    {
      id: 33,
      name: "Linen Cushion Cover",
      category: "Home Goods",
      price: 28000,
      date: "2024-12-01",
      description: "내추럴한 텍스처가 돋보이는 리넨 쿠션 커버.",
      image: "/assets/images/product_cushion.png",
      rating: 4.7,
      reviews: 34,
      badge: "Home Goods"
    },
    {
      id: 34,
      name: "Modern Wall Clock",
      category: "Home Goods",
      price: 95000,
      date: "2024-11-30",
      description: "시간을 미학적으로 보여주는 미니멀 벽시계.",
      image: "/assets/images/product_clock.png",
      rating: 4.8,
      reviews: 19,
      badge: "Home Goods"
    },
    {
      id: 35,
      name: "Canvas Utility Pouch",
      category: "Home Goods",
      price: 22000,
      date: "2024-11-29",
      description: "소지품을 깔끔하게 휴대할 수 있는 캔버스 파우치.",
      image: "/assets/images/product_pouch.png",
      rating: 4.4,
      reviews: 82,
      badge: "Home Goods"
    },
    {
      id: 36,
      name: "Glass Reed Diffuser",
      category: "Home Goods",
      price: 38000,
      date: "2024-11-28",
      description: "공간에 은은한 향기를 채우는 리드 디퓨저.",
      image: "/assets/images/product_diffuser.png",
      rating: 4.7,
      reviews: 64,
      badge: "Home Goods"
    },
    {
      id: 37,
      name: "Premium Leather Keychain",
      category: "Home Goods",
      price: 25000,
      date: "2024-11-27",
      description: "세련된 감각의 천연 가죽 키홀더.",
      image: "/assets/images/product_keychain.png",
      rating: 4.8,
      reviews: 121,
      badge: "NEW"
    }
  ];


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
                  <p className="text-stone-500 text-sm">한정 수량으로 매일 정오에 찾아오는 특별한 기회.</p>
                </div>
                <button
                  onClick={() => setView('all_products')}
                  className="text-sm font-medium border-b border-black pb-1 hover:text-stone-500 hover:border-stone-500 transition-colors"
                >
                  전체 보기
                </button>
              </div>

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
            </section>
          </>
        );
      case 'all_products':
        return <ProductListPage products={products} onProductClick={handleProductClick} />;
      case 'product_detail':
        return selectedProduct ? (
          <ProductDetailPage
            product={selectedProduct}
            onBack={() => setView('all_products')}
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
                <span className="text-sm font-medium">홈으로 돌아가기</span>
              </button>

              <h2 className="text-4xl font-bold tracking-tight mb-12 text-stone-900 text-left">{getPageTitle()}</h2>

              <div className="bg-white p-10 rounded-[40px] shadow-sm border border-stone-100">
                {renderDetailContent()}
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  const renderDetailContent = () => {
    switch (view) {
      case 'order':
        return (
          <div className="space-y-12 text-left">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-stone-400 uppercase tracking-widest">주문번호 20241212-001</span>
              <span className="text-xs bg-blue-50 text-blue-600 px-3 py-1 rounded-full font-bold">배송 중</span>
            </div>
            <div className="flex space-x-8 items-center">
              <div className="w-32 h-32 bg-stone-100 rounded-3xl overflow-hidden">
                <img src="https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&q=80&w=800" className="w-full h-full object-cover" />
              </div>
              <div>
                <h4 className="text-xl font-bold mb-2">Urban Tech Runner v.1</h4>
                <p className="text-stone-400 text-sm mb-4">Size: 270 | Color: Off-white</p>
                <p className="font-bold">₩129,000</p>
              </div>
            </div>
            <div className="pt-8 border-t border-stone-50 flex justify-between items-center text-sm">
              <span className="text-stone-500">12월 14일(토) 도착 예정</span>
              <button className="text-stone-900 font-bold border-b border-black">배송 상세 보기</button>
            </div>
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
      case 'order': return '주문 내역';
      case 'benefit': return '나의 혜택';
      case 'activity': return '리뷰 관리';
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
      />


      {renderContent()}

      <Footer />
    </div>
  );
}

export default App;

