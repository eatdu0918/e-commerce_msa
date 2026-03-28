import { useState } from 'react';
import { Toaster } from 'react-hot-toast';
import { Outlet, useNavigate } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';
import CartModal from '../cart/CartModal';
import LoginModal from '../auth/LoginModal';
import SignupModal from '../auth/SignupModal';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { getMyProfile } from '../../api/services/user';
import { getCart } from '../../api/services/cart';
import { getRootCategories } from '../../api/services/category';
import { useTranslation } from 'react-i18next';
import { catalogCategoryName } from '../../lib/catalogLocale';
import { AUTH_STORAGE_KEYS, clearAuthStorage, getStoredAccessToken } from '../../lib/authStorage';

export default function RootLayout() {
    const { i18n } = useTranslation();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const [isLoginOpen, setIsLoginOpen] = useState(false);
    const [isSignupOpen, setIsSignupOpen] = useState(false);
    const [isCartOpen, setIsCartOpen] = useState(false);
    const [activeCategoryKey, setActiveCategoryKey] = useState('all');

    const { data: categoriesData } = useQuery({
        queryKey: ['categories', 'root', i18n.language],
        queryFn: getRootCategories,
        staleTime: Infinity,
        retry: false,
    });
    const categoryNavItems =
        categoriesData?.map((c) => ({
            key: c.name,
            label: catalogCategoryName(c, i18n.language),
        })) ?? [];

    // Check auth
    const { data: user } = useQuery({
        queryKey: ['user'],
        queryFn: getMyProfile,
        retry: false,
        refetchOnWindowFocus: false,
        enabled: !!getStoredAccessToken(),
        initialData: () => {
            const savedUser = localStorage.getItem(AUTH_STORAGE_KEYS.user);
            return savedUser ? JSON.parse(savedUser) : undefined;
        },
    });

    const { data: cart } = useQuery({
        queryKey: ['cart', i18n.language],
        queryFn: getCart,
        enabled: !!user,
    });

    const handleCategoryChange = (key: string) => {
        setActiveCategoryKey(key);
        if (key === 'all') {
            navigate('/');
        } else {
            navigate(`/?category=${encodeURIComponent(key)}`);
        }
    };

    return (
        <div className="antialiased min-h-screen flex flex-col">
            <Header
                activeCategoryKey={activeCategoryKey}
                onSelectCategory={handleCategoryChange}
                categoryNavItems={categoryNavItems}
                user={user}
                onLoginClick={() => setIsLoginOpen(true)}
                onLogoutClick={() => {
                    clearAuthStorage();
                    queryClient.setQueryData(['user'], null);
                    queryClient.invalidateQueries({ queryKey: ['cart'] });
                    navigate('/');
                }}
                onCartClick={() => setIsCartOpen(true)}
                cartCount={cart?.totalItemCount || 0}
            />

            <main className="flex-grow">
                <Outlet context={{
                    openLogin: () => setIsLoginOpen(true),
                    openCart: () => setIsCartOpen(true)
                }} />
            </main>

            <Footer />

            <CartModal
                isOpen={isCartOpen}
                onClose={() => setIsCartOpen(false)}
                onCheckout={() => {
                    setIsCartOpen(false);
                    navigate('/checkout');
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
                    navigate('/signup');
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
            <Toaster position="top-center" />
        </div>
    );
}
