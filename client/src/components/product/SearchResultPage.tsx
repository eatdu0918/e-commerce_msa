import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../../api/services/product';
import ProductCard from './ProductCard';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { Search } from 'lucide-react';

export default function SearchResultPage() {
    const [searchParams] = useSearchParams();
    const query = searchParams.get('q') || '';
    const { t } = useTranslation();
    const [page, setPage] = useState(0);

    const { data, isLoading, isError } = useQuery({
        queryKey: ['products', 'search', query, page],
        queryFn: () => fetchProducts(page, 12, undefined, undefined, query),
        enabled: !!query,
        staleTime: 1000 * 60 * 5, // 5 minutes
    });

    // Reset page when query changes
    useEffect(() => {
        setPage(0);
    }, [query]);

    if (!query) {
        return (
            <div className="min-h-screen pt-24 pb-12 flex flex-col items-center justify-center text-center px-4">
                <Search size={48} className="text-stone-300 mb-4" />
                <h2 className="text-2xl font-bold mb-2">{t('search.enter_keyword') || 'Please enter a keyword'}</h2>
                <p className="text-stone-500">{t('search.enter_keyword_desc') || 'Type something in the search bar to find products.'}</p>
            </div>
        );
    }

    return (
        <div className="min-h-screen pt-12 pb-24 bg-[#f9f7f2]">
            <Helmet>
                <title>Search: {query} | Urban Threads</title>
            </Helmet>

            <div className="max-w-7xl mx-auto px-6">
                <div className="mb-8">
                    <h2 className="text-3xl font-bold">
                        "{query}" {t('search.results_for') || 'Search Results'}
                    </h2>
                    <p className="text-stone-500 mt-2">
                        {isLoading ? 'Searching...' : `${data?.totalElements || 0} products found`}
                    </p>
                </div>

                {isLoading ? (
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-x-8 gap-y-12">
                        {[...Array(8)].map((_, i) => (
                            <div key={i} className="animate-pulse space-y-4">
                                <div className="aspect-[3/4] bg-stone-200 rounded-2xl" />
                                <div className="h-4 bg-stone-200 w-3/4 rounded" />
                                <div className="h-4 bg-stone-200 w-1/2 rounded" />
                            </div>
                        ))}
                    </div>
                ) : isError ? (
                    <div className="text-center py-20">
                        <p className="text-red-500">{t('common.error_occurred') || 'An error occurred while searching.'}</p>
                    </div>
                ) : data?.content.length === 0 ? (
                    <div className="text-center py-32 bg-white rounded-[30px] border border-stone-100">
                        <Search size={48} className="text-stone-200 mx-auto mb-4" />
                        <h3 className="text-xl font-bold mb-2">{t('search.no_results') || 'No results found'}</h3>
                        <p className="text-stone-400">Try checking your spelling or use different keywords.</p>
                    </div>
                ) : (
                    <>
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-x-8 gap-y-12 mb-16">
                            {data?.content.map((product) => (
                                <ProductCard key={product.id} product={product} />
                            ))}
                        </div>

                        {/* Simple Pagination */}
                        {data && data.totalPages > 1 && (
                            <div className="flex justify-center space-x-2">
                                <button
                                    onClick={() => setPage(p => Math.max(0, p - 1))}
                                    disabled={page === 0}
                                    className="px-4 py-2 rounded-lg border border-stone-200 disabled:opacity-30 hover:bg-stone-50 transition-colors"
                                >
                                    Previous
                                </button>
                                <span className="px-4 py-2 font-medium">
                                    {page + 1} / {data.totalPages}
                                </span>
                                <button
                                    onClick={() => setPage(p => Math.min(data.totalPages - 1, p + 1))}
                                    disabled={page >= data.totalPages - 1}
                                    className="px-4 py-2 rounded-lg border border-stone-200 disabled:opacity-30 hover:bg-stone-50 transition-colors"
                                >
                                    Next
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
}
