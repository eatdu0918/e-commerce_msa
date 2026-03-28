import { useState, useEffect } from 'react';
import { Toaster } from 'react-hot-toast';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
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

export default function RootLayout() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();
    const queryClient = useQueryClient();

    const [isLoginOpen, setIsLoginOpen] = useState(false);
    const [isSignupOpen, setIsSignupOpen] = useState(false);
    const [isCartOpen, setIsCartOpen] = useState(false);
    const [category, setCategory] = useState(t('common.category_all'));

    const { data: categoriesData } = useQuery({
        queryKey: ['categories', 'root'],
        queryFn: getRootCategories,
        staleTime: Infinity,
        retry: false,
    });
    const categories = categoriesData?.map(c => c.name) ?? [];

    // Check auth
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

    const { data: cart } = useQuery({
        queryKey: ['cart'],
        queryFn: getCart,
        enabled: !!user,
    });

    // Handle category change navigation
    const handleCategoryChange = (newCategory: string) => {
        setCategory(newCategory);
        if (newCategory === t('common.category_all')) {
            navigate('/');
        } else {
            // Simple mapping for demo. In real app, use IDs or slugs
            navigate(`/?category=${encodeURIComponent(newCategory)}`);
        }
    };

    return (
        <div className="antialiased min-h-screen flex flex-col">
            <Header
                category={category}
                setCategory={handleCategoryChange}
                categories={categories}
                user={user}
                onLoginClick={() => setIsLoginOpen(true)}
                onLogoutClick={() => {
                    sessionStorage.removeItem('accessToken');
                    sessionStorage.removeItem('user');
                    queryClient.setQueryData(['user'], null);
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
