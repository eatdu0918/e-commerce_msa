import { useState, useEffect } from 'react';

import ProductCard from './ProductCard';
import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../../api/services/product';



import { useTranslation } from 'react-i18next';

import { useNavigate, useSearchParams } from 'react-router-dom';
import { Helmet } from 'react-helmet-async';

export default function ProductListPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const keyword = searchParams.get('keyword') || undefined;

    const [currentFilter, setCurrentFilter] = useState('all');
    const [currentSort, setCurrentSort] = useState('new');
    const [page, setPage] = useState(0);
    const pageSize = 12;

    // Reset page when filter changes
    const handleFilterChange = (category: string) => {
        setCurrentFilter(category);
        setPage(0);
    };

    // Reset page when sort changes
    const handleSortChange = (sort: string) => {
        setCurrentSort(sort);
        setPage(0);
    };

    // Reset page when keyword changes
    useEffect(() => {
        setPage(0);
    }, [keyword]);

    // Map frontend sort to backend sort
    const getBackendSort = (sort: string) => {
        switch (sort) {
            case 'low': return 'price,asc';
            case 'high': return 'price,desc';
            case 'new': return 'createdAt,desc';
            default: return 'createdAt,desc';
        }
    };

    // Map frontend category to backend ID (Hardcoded for now based on V7/V5 data)
    // In a real app, we should fetch categories from API
    const getCategoryId = (filter: string) => {
        if (filter === 'all') return undefined;
        // This mapping must match the database IDs or we need to fetch categories first
        // Assuming: 1: Electronics, 2: Clothing, 3: Accessories
        // But names in DB are 'Electronics', 'Clothing', 'Accessories'
        // And frontend uses 'Apparel', 'Home Goods', 'Footwear' which are NOT in DB?
        // Let's check App.tsx categories: ['NEW ARRIVALS', 'HOME GOODS']
        // The previous ProductListPage had hardcoded filters: ['all', 'Apparel', 'Home Goods', 'Footwear']
        // Backend categories: 'Electronics', 'Clothing', 'Accessories'
        // We should probably align these. Let's align frontend to backend for now.
        switch (filter) {
            case 'Electronics': return 1;
            case 'Clothing': return 2;
            case 'Accessories': return 3;
            default: return undefined;
        }
    };

    const { data: productsPage, isLoading } = useQuery({
        queryKey: ['products', page, currentFilter, currentSort, keyword],
        queryFn: () => fetchProducts(page, pageSize, getCategoryId(currentFilter), getBackendSort(currentSort), keyword),
    });

    const products = productsPage?.content || [];
    const totalPages = productsPage?.totalPages || 0;

    return (
        <div className="min-h-screen py-20 bg-[#f9f7f2]">
            <Helmet>
                <title>{keyword ? `Search: ${keyword} | Sparta Shop` : 'Shop All | Sparta Shop'}</title>
                <meta name="description" content="Explore our complete collection of premium products." />
            </Helmet>
            <div className="max-w-7xl mx-auto px-6">
                <header className="mb-16 text-left">
                    <h2 className="text-5xl font-bold tracking-tight mb-4">{t('common.shop_all')}</h2>
                    <p className="text-stone-400">{t('common.shop_subtitle')}</p>
                </header>

                <div className="flex flex-col md:flex-row justify-between items-start md:items-center space-y-4 md:space-y-0 mb-12 border-b border-stone-200 pb-8">
                    <div className="flex space-x-4 overflow-x-auto no-scrollbar pb-2 md:pb-0 w-full md:w-auto">
                        {['all', 'Electronics', 'Clothing', 'Accessories'].map((cat) => (
                            <button
                                key={cat}
                                onClick={() => handleFilterChange(cat)}
                                className={`px-6 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${currentFilter === cat
                                    ? 'bg-black text-white'
                                    : 'bg-stone-100 text-stone-600 hover:bg-stone-200'
                                    }`}
                            >
                                {cat === 'all' ? t('common.category_all') : cat === 'Electronics' ? t('common.category_electronics') : cat === 'Clothing' ? t('common.category_clothing') : t('common.category_accessories')}
                            </button>
                        ))}
                    </div>
                    <div className="flex items-center space-x-6 w-full md:w-auto justify-between">
                        <span className="text-xs text-stone-400">
                            <span className="font-medium text-stone-600">{productsPage?.totalElements || 0}</span> {t('common.products_count', { count: productsPage?.totalElements || 0 }).replace(/[0-9]+\s/, '')}
                        </span>
                        <select
                            value={currentSort}
                            onChange={(e) => handleSortChange(e.target.value)}
                            className="bg-transparent text-sm font-medium border-none focus:ring-0 cursor-pointer outline-none"
                        >
                            <option value="new">{t('common.sort_newest')}</option>
                            <option value="low">{t('common.sort_price_low')}</option>
                            <option value="high">{t('common.sort_price_high')}</option>
                        </select>
                    </div>
                </div>

                {isLoading ? (
                    <div className="flex justify-center items-center h-64">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-12">
                            {products.map(product => (
                                <div key={product.id} onClick={() => navigate(`/product/${product.id}`)} className="cursor-pointer">
                                    <ProductCard product={product} />
                                </div>
                            ))}
                        </div>
                        {products.length === 0 && (
                            <div className="text-center py-20 text-stone-500">
                                {t('common.no_products')}
                            </div>
                        )}
                    </>
                )}

                {/* Pagination Controls */}
                <div className="mt-20 flex justify-center space-x-2">
                    <button
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="w-10 h-10 flex items-center justify-center rounded-full border border-stone-200 text-stone-400 hover:border-black hover:text-black transition-colors disabled:opacity-30 disabled:hover:border-stone-200 disabled:hover:text-stone-400"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="m15 18-6-6 6-6" />
                        </svg>
                    </button>
                    {/* Simple Pagination: Show current page and total pages */}
                    <div className="flex items-center space-x-2 px-4">
                        <span className="text-sm font-medium">{page + 1} / {totalPages === 0 ? 1 : totalPages}</span>
                    </div>
                    <button
                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                        disabled={page >= totalPages - 1}
                        className="w-10 h-10 flex items-center justify-center rounded-full border border-stone-200 text-stone-400 hover:border-black hover:text-black transition-colors disabled:opacity-30 disabled:hover:border-stone-200 disabled:hover:text-stone-400"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="m9 18 6-6-6-6" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    );
}
