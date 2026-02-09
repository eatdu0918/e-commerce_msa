import { Search, ShoppingBag, Menu, X, User as UserIcon, Bell, ChevronRight } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import type { UserResponse } from '../../api/services/user';
import LanguageSwitcher from '../LanguageSwitcher';
import { useTranslation } from 'react-i18next';

interface HeaderProps {
    category: string;
    setCategory: (category: string) => void;
    categories: string[];
    onNavigate: (view: string) => void;
    view?: string;
    user?: UserResponse;
    onLoginClick: () => void;
    onLogoutClick: () => void;
    onCartClick: () => void;
    cartCount?: number;
}

export default function Header({ category, setCategory, categories, onNavigate, view, user, onLoginClick, onLogoutClick, onCartClick, cartCount = 0 }: HeaderProps) {
    const { t } = useTranslation();

    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [userDropdownOpen, setUserDropdownOpen] = useState(false);
    const [notificationOpen, setNotificationOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const notificationRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setUserDropdownOpen(false);
            }
            if (notificationRef.current && !notificationRef.current.contains(event.target as Node)) {
                setNotificationOpen(false);
            }
        }
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <nav className="sticky top-0 z-[100] bg-white/80 backdrop-blur-md border-b border-stone-200">
            <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
                <div
                    className="text-xl font-bold tracking-tighter cursor-pointer"
                    onClick={() => {
                        setCategory(t('common.category_all'));
                        onNavigate('home');
                    }}
                >
                    URBAN THREADS
                </div>

                <div className="hidden md:flex space-x-8 text-sm font-medium text-stone-600">
                    <button
                        onClick={() => onNavigate('all_products')}
                        className={`hover:text-black transition-colors ${view === 'all_products' ? 'text-black font-bold' : ''}`}
                    >
                        SHOP ALL
                    </button>
                    {categories.map((cat) => (
                        <button
                            key={cat}
                            onClick={() => {
                                setCategory(cat);
                                onNavigate('home');
                            }}
                            className={`hover:text-black transition-colors ${category === cat ? 'text-black' : ''
                                }`}
                        >
                            {cat}
                        </button>
                    ))}
                </div>


                <div className="flex items-center space-x-1 md:space-x-4">
                    <LanguageSwitcher />
                    <button className="p-2 hover:bg-stone-100 rounded-full transition-colors">
                        <Search size={20} strokeWidth={2} />
                    </button>

                    {/* User Dropdown */}
                    <div className="relative" ref={dropdownRef}>
                        {user ? (
                            <>
                                <button
                                    className="p-2 hover:bg-stone-100 rounded-full transition-colors flex items-center"
                                    onClick={() => setUserDropdownOpen(!userDropdownOpen)}
                                >
                                    <UserIcon size={20} strokeWidth={2} />
                                </button>

                                {userDropdownOpen && (
                                    <div className="absolute right-0 mt-3 w-80 bg-white border border-stone-200 rounded-2xl shadow-xl overflow-hidden z-[110]">
                                        <div className="p-6 bg-stone-50 border-b border-stone-100">
                                            <div className="flex items-center space-x-3">
                                                <div className="w-12 h-12 bg-stone-200 rounded-full flex items-center justify-center text-stone-500 font-bold">
                                                    {user.username?.substring(0, 2).toUpperCase() || '??'}
                                                </div>
                                                <div className="text-left">
                                                    <p className="font-bold text-sm">{user.username} 님</p>
                                                    <p className="text-xs text-stone-400">{user.role}</p>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="max-h-[400px] overflow-y-auto no-scrollbar">
                                            <div className="p-4 border-b border-stone-50">
                                                <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-3 text-left">{t('common.my_shopping')}</h4>
                                                <ul className="space-y-3 text-sm text-left">
                                                    <li
                                                        className="flex justify-between items-center cursor-pointer hover:text-black transition-colors"
                                                        onClick={() => {
                                                            onNavigate('mypage');
                                                            setUserDropdownOpen(false);
                                                        }}
                                                    >
                                                        <span className="font-bold text-blue-600">{t('common.my_page')}</span>
                                                        <ChevronRight size={14} className="text-stone-300" />
                                                    </li>
                                                    <li
                                                        className="flex justify-between items-center cursor-pointer hover:text-blue-600 transition-colors"
                                                        onClick={() => {
                                                            onNavigate('order');
                                                            setUserDropdownOpen(false);
                                                        }}
                                                    >
                                                        <span>{t('common.order_tracking')}</span>
                                                        <span className="text-xs bg-stone-100 px-2 py-0.5 rounded text-stone-500 font-medium">3건</span>
                                                    </li>
                                                    <li
                                                        className="flex justify-between items-center cursor-pointer hover:text-red-600 transition-colors"
                                                        onClick={() => {
                                                            onNavigate('wishlist');
                                                            setUserDropdownOpen(false);
                                                        }}
                                                    >
                                                        <span>{t('common.wishlist') || '찜한 상품'}</span>
                                                        <span className="text-xs bg-stone-100 px-2 py-0.5 rounded text-stone-500 font-medium">{cartCount > 0 ? '●' : ''}</span>
                                                    </li>
                                                </ul>
                                            </div>
                                            <div className="p-4 text-left">
                                                <h4 className="text-[11px] font-bold text-stone-400 uppercase tracking-widest mb-3">{t('common.my_info')}</h4>
                                                <ul className="space-y-3 text-sm">
                                                    <li
                                                        className="cursor-pointer hover:text-black transition-colors"
                                                        onClick={() => {
                                                            onNavigate('edit_profile');
                                                            setUserDropdownOpen(false);
                                                        }}
                                                    >
                                                        {t('common.edit_profile')}
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                        <button
                                            onClick={() => {
                                                onLogoutClick();
                                                setUserDropdownOpen(false);
                                            }}
                                            className="w-full py-4 bg-stone-900 text-white text-xs font-bold hover:bg-black transition-colors"
                                        >
                                            {t('common.logout')}
                                        </button>
                                    </div>
                                )}
                            </>
                        ) : (
                            <button
                                onClick={onLoginClick}
                                className="text-sm font-bold hover:text-stone-500 transition-colors"
                            >
                                {t('common.login')}
                            </button>
                        )}
                    </div>

                    <div className="relative" ref={notificationRef}>
                        <button
                            onClick={() => setNotificationOpen(!notificationOpen)}
                            className="p-2 hover:bg-stone-100 rounded-full transition-colors relative"
                        >
                            <Bell size={20} strokeWidth={2} />
                            <span className="absolute top-2 right-2 w-1.5 h-1.5 bg-red-500 rounded-full"></span>
                        </button>

                        {notificationOpen && (
                            <div className="absolute right-0 mt-3 w-80 bg-white border border-stone-200 rounded-2xl shadow-xl overflow-hidden z-[110]">
                                <div className="p-5 border-b border-stone-100 bg-stone-50/50 flex justify-between items-center">
                                    <h3 className="font-bold text-sm tracking-tight">{t('notification.title')}</h3>
                                    <button className="text-[10px] font-bold text-stone-400 hover:text-black transition-colors">
                                        {t('notification.view_all')}
                                    </button>
                                </div>
                                <div className="p-10 text-center">
                                    <div className="w-12 h-12 bg-stone-50 rounded-full flex items-center justify-center mx-auto mb-4 text-stone-200">
                                        <Bell size={24} />
                                    </div>
                                    <p className="text-xs text-stone-400 font-medium">{t('notification.empty')}</p>
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="relative">
                        <button
                            onClick={onCartClick}
                            className="p-2 hover:bg-stone-100 rounded-full transition-colors relative"
                        >
                            <ShoppingBag size={20} strokeWidth={2} />
                            {cartCount > 0 && (
                                <span className="absolute top-1 right-1 bg-red-500 text-white text-[10px] w-4 h-4 flex items-center justify-center rounded-full font-bold">
                                    {cartCount}
                                </span>
                            )}
                        </button>
                    </div>

                    <button
                        className="md:hidden p-2 hover:bg-stone-100 rounded-full transition-colors"
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                    >
                        {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
                    </button>
                </div>
            </div>

            {/* Mobile Menu */}
            {mobileMenuOpen && (
                <div className="md:hidden bg-white border-t border-stone-200 py-4 px-6">
                    <div className="flex flex-col space-y-4 text-sm font-medium text-stone-600 text-left">
                        <button
                            onClick={() => {
                                onNavigate('all_products');
                                setMobileMenuOpen(false);
                            }}
                            className={`hover:text-black transition-colors ${view === 'all_products' ? 'text-black' : ''}`}
                        >
                            SHOP ALL
                        </button>
                        {categories.map((cat) => (
                            <button
                                key={cat}
                                onClick={() => {
                                    setCategory(cat);
                                    setMobileMenuOpen(false);
                                    onNavigate('home');
                                }}
                                className={`text-left hover:text-black transition-colors ${category === cat ? 'text-black' : ''
                                    }`}
                            >
                                {cat}
                            </button>
                        ))}
                    </div>

                </div>
            )}
        </nav>
    );
}
