import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Helmet } from 'react-helmet-async';
import Hero from './Hero';
import CategoryGrid from './CategoryGrid';
import ProductCard from '../product/ProductCard';
import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../../api/services/product';
import type { Product } from '../../types/product';
import { useTranslation } from 'react-i18next';

export default function HomePage() {
    const { t, i18n } = useTranslation();
    const navigate = useNavigate();
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

    const { data: productsPage, isLoading } = useQuery({
        queryKey: ['products', 'home', i18n.language],
        queryFn: () => fetchProducts(0, 4),
    });

    const products: Product[] = productsPage?.content || [];

    return (
        <>
            <Helmet>
                <title>{t('homePage.title')}</title>
                <meta name="description" content={t('homePage.description')} />
            </Helmet>
            <Hero />
            <CategoryGrid />
            <section className="max-w-7xl mx-auto px-6 py-20 border-t border-stone-200">
                <div className="flex justify-between items-end mb-10 text-left">
                    <div>
                        <h2 className="text-2xl font-bold tracking-tight mb-2">{t('home.daily_drops')}</h2>
                        <p className="text-stone-500 text-sm">{t('homePage.section_subtitle')}</p>
                    </div>
                    <button
                        onClick={() => navigate('/shop')}
                        className="text-sm font-medium border-b border-black pb-1 hover:text-stone-500 hover:border-stone-500 transition-colors"
                    >
                        {t('home.view_all')}
                    </button>
                </div>

                {isLoading ? (
                    <div className="flex justify-center items-center h-40">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                        {products.slice(0, 3).map((product, index) => (
                            <div key={product.id} onClick={() => navigate(`/product/${product.id}`)} className="cursor-pointer">
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
}
