import { useState, useEffect, type FormEvent } from 'react';
import ProductCard from './ProductCard';
import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../../api/services/product';
import { getAllCategories } from '../../api/services/category';
import { useTranslation } from 'react-i18next';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Helmet } from 'react-helmet-async';
import { Button } from '../ui/Button';
import { Input } from '../ui/Input';
import { Skeleton } from '../ui/Skeleton';
import { Search } from 'lucide-react';

export default function ProductListPage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const keyword = searchParams.get('keyword') || undefined;

    const [currentFilter, setCurrentFilter] = useState('all');
    const [currentSort, setCurrentSort] = useState('new');
    const [page, setPage] = useState(0);
    const [searchTerm, setSearchTerm] = useState(keyword || '');
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

    // Sync searchTerm with URL keyword
    useEffect(() => {
        setSearchTerm(keyword || '');
        setPage(0);
    }, [keyword]);

    const handleSearch = (e: FormEvent) => {
        e.preventDefault();
        if (searchTerm.trim()) {
            setSearchParams({ keyword: searchTerm });
        } else {
            setSearchParams({});
        }
    };

    // Map frontend sort to backend sort
    const getBackendSort = (sort: string) => {
        switch (sort) {
            case 'low': return 'price,asc';
            case 'high': return 'price,desc';
            case 'new': return 'createdAt,desc';
            default: return 'createdAt,desc';
        }
    };

    const { data: categoriesData } = useQuery({
        queryKey: ['categories'],
        queryFn: getAllCategories,
        staleTime: Infinity,
        retry: false,
    });

    const getCategoryId = (filter: string) => {
        if (filter === 'all') return undefined;
        return categoriesData?.find(c => c.name === filter)?.id;
    };

    const filterOptions = ['all', ...(categoriesData?.map(c => c.name) ?? [])];

    const { data: productsPage, isLoading, isError, refetch } = useQuery({
        queryKey: ['products', page, currentFilter, currentSort, keyword],
        queryFn: () => fetchProducts(page, pageSize, getCategoryId(currentFilter), getBackendSort(currentSort), keyword),
    });

    const products = productsPage?.content || [];
    const totalPages = productsPage?.totalPages || 0;

    if (isError) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-[#f9f7f2]">
                <div className="text-center p-8 bg-white rounded-3xl shadow-xl max-w-md">
                    <h2 className="text-2xl font-bold text-red-500 mb-4">앗! 문제가 발생했습니다</h2>
                    <p className="text-stone-500 mb-6">상품 정보를 불러오는 중 오류가 발생했습니다. 다시 시도해 주세요.</p>
                    <Button onClick={() => refetch()} variant="primary" className="w-full">다시 시도</Button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen py-20 bg-[#f9f7f2]">
            <Helmet>
                <title>{keyword ? `Search: ${keyword} | Sparta Shop` : 'Shop All | Sparta Shop'}</title>
                <meta name="description" content="Explore our complete collection of premium products." />
            </Helmet>
            <div className="max-w-7xl mx-auto px-6">
                <header className="mb-12 text-left space-y-6">
                    <div>
                        <h2 className="text-5xl font-bold tracking-tight mb-4">{t('common.shop_all')}</h2>
                        <p className="text-stone-400">{t('common.shop_subtitle')}</p>
                    </div>

                    {/* Search Bar */}
                    <form onSubmit={handleSearch} className="max-w-md relative">
                        <Input
                            placeholder={t('common.search_placeholder') || "Search products..."}
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="pr-12"
                        />
                        <button
                            type="submit"
                            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-black transition-colors"
                        >
                            <Search className="h-5 w-5" />
                        </button>
                    </form>
                </header>

                <div className="flex flex-col md:flex-row justify-between items-start md:items-center space-y-4 md:space-y-0 mb-12 border-b border-stone-200 pb-8">
                    <div className="flex space-x-2 overflow-x-auto no-scrollbar pb-2 md:pb-0 w-full md:w-auto">
                        {filterOptions.map((cat) => (
                            <Button
                                key={cat}
                                onClick={() => handleFilterChange(cat)}
                                variant={currentFilter === cat ? 'primary' : 'ghost'}
                                size="sm"
                                className={`rounded-full ${currentFilter === cat ? '' : 'bg-stone-100 text-stone-600 hover:bg-stone-200'}`}
                            >
                                {cat === 'all' ? t('common.category_all') : cat}
                            </Button>
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
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-12">
                        {Array.from({ length: 8 }).map((_, i) => (
                            <div key={i} className="space-y-4">
                                <Skeleton className="h-[300px] w-full rounded-xl" />
                                <div className="space-y-2">
                                    <Skeleton className="h-4 w-[250px]" />
                                    <Skeleton className="h-4 w-[200px]" />
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-12">
                            {products.map(product => (
                                <div key={product.id} onClick={() => navigate(`/product/${product.id}`)} className="cursor-pointer group">
                                    <ProductCard product={product} />
                                </div>
                            ))}
                        </div>
                        {products.length === 0 && (
                            <div className="text-center py-20 text-stone-500">
                                <p className="text-lg mb-4">{t('common.no_products')}</p>
                                <Button variant="outline" onClick={() => {
                                    setSearchParams({});
                                    setSearchTerm('');
                                }}>
                                    Clear Filters
                                </Button>
                            </div>
                        )}
                    </>
                )}

                {/* Pagination Controls */}
                <div className="mt-20 flex justify-center space-x-2">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="w-10 h-10 p-0 rounded-full"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="m15 18-6-6 6-6" />
                        </svg>
                    </Button>
                    <div className="flex items-center space-x-2 px-4">
                        <span className="text-sm font-medium">{page + 1} / {totalPages === 0 ? 1 : totalPages}</span>
                    </div>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                        disabled={page >= totalPages - 1}
                        className="w-10 h-10 p-0 rounded-full"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="m9 18 6-6-6-6" />
                        </svg>
                    </Button>
                </div>
            </div>
        </div>
    );
}
