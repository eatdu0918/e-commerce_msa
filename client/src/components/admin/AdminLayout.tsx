import { Outlet, useNavigate, useLocation, Navigate } from 'react-router-dom';
import ErrorBoundary from '../common/ErrorBoundary';
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
  PackageX,
} from 'lucide-react';
import { useState, useEffect } from 'react';
import { useScrollLock } from '../../hooks/useScrollLock';
import { useTranslation } from 'react-i18next';
import { AUTH_STORAGE_KEYS, isAuthStorageKey } from '../../lib/authStorage';

export default function AdminLayout() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const userRole = localStorage.getItem(AUTH_STORAGE_KEYS.role);

  useScrollLock(sidebarOpen);

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (!isAuthStorageKey(e.key)) return;
      const role = localStorage.getItem(AUTH_STORAGE_KEYS.role);
      if (!role || role !== 'ADMIN') {
        navigate('/admin/login', { replace: true });
      }
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, [navigate]);

  // 자식(관리자 API useQuery)보다 먼저 검사 — useEffect 이후가 아닌 첫 렌더에서 차단
  if (!userRole || userRole !== 'ADMIN') {
    return <Navigate to="/admin/login" replace />;
  }

  const isActive = (path: string) => location.pathname === path;

  const navItems = [
    { icon: LayoutDashboard, label: t('admin.nav_dashboard'), path: '/admin' },
    { icon: Users, label: t('admin.nav_users'), path: '/admin/users' },
    { icon: Package, label: t('admin.nav_products'), path: '/admin/products' },
    { icon: ShoppingBag, label: t('admin.nav_orders'), path: '/admin/orders' },
    { icon: CreditCard, label: t('admin.nav_payments'), path: '/admin/payments' },
    { icon: Ticket, label: t('admin.nav_coupons'), path: '/admin/coupons' },
    { icon: XCircle, label: t('admin.nav_cancels'), path: '/admin/cancels' },
    { icon: PackageX, label: t('admin.nav_returns'), path: '/admin/returns' },
    { icon: RotateCcw, label: t('admin.nav_refunds'), path: '/admin/refunds' },
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
            <span className="text-sm font-medium">{t('admin.back_to_site')}</span>
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
                <span className="text-sm font-medium">{t('admin.back_to_site')}</span>
              </button>
            </nav>
          </div>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 md:ml-64 p-6 pt-24 md:pt-6">
        <div className="max-w-6xl mx-auto">
          <ErrorBoundary key={location.pathname}>
            <Outlet />
          </ErrorBoundary>
        </div>
      </main>
    </div>
  );
}
