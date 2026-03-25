import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  ShoppingBag,
  LogOut,
  Menu,
  X,
  Users,
  CreditCard,
  Ticket,
  XCircle,
  RotateCcw,
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { useScrollLock } from '../../hooks/useScrollLock';

export default function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useScrollLock(sidebarOpen);

  useEffect(() => {
    const userRole = sessionStorage.getItem('role');
    if (userRole !== 'ROLE_ADMIN') {
      alert('관리자 권한이 필요합니다.');
      navigate('/');
    }
  }, [navigate]);

  const isActive = (path: string) => location.pathname === path;

  const navItems = [
    { icon: LayoutDashboard, label: '대시보드', path: '/admin' },
    { icon: Users, label: '사용자', path: '/admin/users' },
    { icon: Package, label: '상품', path: '/admin/products' },
    { icon: ShoppingBag, label: '주문', path: '/admin/orders' },
    { icon: CreditCard, label: '결제', path: '/admin/payments' },
    { icon: Ticket, label: '쿠폰', path: '/admin/coupons' },
    { icon: XCircle, label: '취소', path: '/admin/cancels' },
    { icon: RotateCcw, label: '환불', path: '/admin/refunds' },
  ];

  return (
    <div className="min-h-screen bg-stone-50 flex">
      {/* Sidebar - Desktop */}
      <aside className="hidden md:flex flex-col w-64 bg-white border-r border-stone-200 fixed inset-y-0 z-50">
        <div className="h-16 flex items-center px-6 border-b border-stone-100">
          <span className="text-xl font-bold tracking-tighter">UT ADMIN</span>
        </div>

        <nav className="flex-1 p-4 space-y-2">
          {navItems.map((item) => (
            <button
              key={item.path}
              onClick={() => navigate(item.path)}
              className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all ${isActive(item.path)
                ? 'bg-black text-white font-bold shadow-md shadow-black/10'
                : 'text-stone-500 hover:bg-stone-100 hover:text-black'
                }`}
            >
              <item.icon size={20} />
              <span className="text-sm">{item.label}</span>
            </button>
          ))}
        </nav>

        <div className="p-4 border-t border-stone-100">
          <button
            onClick={() => navigate('/')}
            className="w-full flex items-center space-x-3 px-4 py-3 text-stone-500 hover:text-red-500 hover:bg-red-50 rounded-xl transition-all"
          >
            <LogOut size={20} />
            <span className="text-sm font-medium">사용자 페이지로</span>
          </button>
        </div>
      </aside>

      {/* Mobile Header */}
      <div className="md:hidden fixed top-0 w-full bg-white border-b border-stone-200 z-40 h-16 flex items-center justify-between px-4">
        <span className="text-lg font-bold">UT ADMIN</span>
        <button onClick={() => setSidebarOpen(!sidebarOpen)} className="p-2">
          {sidebarOpen ? <X /> : <Menu />}
        </button>
      </div>

      {/* Mobile Sidebar Overlay */}
      {sidebarOpen && (
        <div className="md:hidden fixed inset-0 z-50 bg-black/50" onClick={() => setSidebarOpen(false)}>
          <div className="w-64 h-full bg-white p-4" onClick={e => e.stopPropagation()}>
            <nav className="space-y-2 mt-12">
              {navItems.map((item) => (
                <button
                  key={item.path}
                  onClick={() => {
                    navigate(item.path);
                    setSidebarOpen(false);
                  }}
                  className={`w-full flex items-center space-x-3 px-4 py-3 rounded-xl transition-all ${isActive(item.path)
                    ? 'bg-black text-white font-bold'
                    : 'text-stone-500 hover:bg-stone-100'
                    }`}
                >
                  <item.icon size={20} />
                  <span className="text-sm">{item.label}</span>
                </button>
              ))}
              <button
                onClick={() => navigate('/')}
                className="w-full flex items-center space-x-3 px-4 py-3 text-stone-500 hover:text-red-500 mt-8"
              >
                <LogOut size={20} />
                <span className="text-sm font-medium">사용자 페이지로</span>
              </button>
            </nav>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 md:ml-64 p-6 pt-24 md:pt-6">
        <div className="max-w-6xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
