import { useState } from 'react';
import type { Product } from '../../types/product';
import ProductCard from './ProductCard';

interface ProductListPageProps {
    products: Product[];
    onProductClick: (product: Product) => void;
}

export default function ProductListPage({ products, onProductClick }: ProductListPageProps) {
    const [currentFilter, setCurrentFilter] = useState('all');
    const [currentSort, setCurrentSort] = useState('new');

    const filteredProducts = products
        .filter(p => currentFilter === 'all' || p.category === currentFilter)
        .sort((a, b) => {
            if (currentSort === 'low') return a.price - b.price;
            if (currentSort === 'high') return b.price - a.price;
            return new Date(b.date).getTime() - new Date(a.date).getTime();
        });

    return (
        <div className="min-h-screen py-20 bg-[#f9f7f2]">
            <div className="max-w-7xl mx-auto px-6">
                <header className="mb-16 text-left">
                    <h2 className="text-5xl font-bold tracking-tight mb-4">SHOP ALL</h2>
                    <p className="text-stone-400">당신을 위해 큐레이션된 미니멀리즘 아이템들을 만나보세요.</p>
                </header>

                <div className="flex flex-col md:flex-row justify-between items-start md:items-center space-y-4 md:space-y-0 mb-12 border-b border-stone-200 pb-8">
                    <div className="flex space-x-4 overflow-x-auto no-scrollbar pb-2 md:pb-0 w-full md:w-auto">
                        {['all', 'Apparel', 'Home Goods', 'Footwear'].map((cat) => (
                            <button
                                key={cat}
                                onClick={() => setCurrentFilter(cat)}
                                className={`px-6 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-colors ${currentFilter === cat
                                        ? 'bg-black text-white'
                                        : 'bg-stone-100 text-stone-600 hover:bg-stone-200'
                                    }`}
                            >
                                {cat === 'all' ? '전체' : cat === 'Apparel' ? '의류' : cat === 'Home Goods' ? '홈 굿즈' : '신발'}
                            </button>
                        ))}
                    </div>
                    <div className="flex items-center space-x-6 w-full md:w-auto justify-between">
                        <span className="text-xs text-stone-400">
                            <span className="font-medium text-stone-600">{filteredProducts.length}</span> Products
                        </span>
                        <select
                            value={currentSort}
                            onChange={(e) => setCurrentSort(e.target.value)}
                            className="bg-transparent text-sm font-medium border-none focus:ring-0 cursor-pointer outline-none"
                        >
                            <option value="new">신상품순</option>
                            <option value="low">낮은 가격순</option>
                            <option value="high">높은 가격순</option>
                        </select>
                    </div>
                </div>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-x-6 gap-y-12">
                    {filteredProducts.map(product => (
                        <div key={product.id} onClick={() => onProductClick(product)} className="cursor-pointer">
                            <ProductCard product={product} />
                        </div>
                    ))}
                </div>

                <div className="mt-20 flex justify-center space-x-2">
                    <button className="w-10 h-10 flex items-center justify-center rounded-full border border-stone-200 text-stone-400 hover:border-black hover:text-black transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="m15 18-6-6 6-6" />
                        </svg>
                    </button>
                    <button className="w-10 h-10 flex items-center justify-center rounded-full bg-black text-white text-sm font-bold">1</button>
                    <button className="w-10 h-10 flex items-center justify-center rounded-full border border-stone-200 text-stone-600 text-sm hover:border-black transition-colors">2</button>
                    <button className="w-10 h-10 flex items-center justify-center rounded-full border border-stone-200 text-stone-400 hover:border-black hover:text-black transition-colors">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="m9 18 6-6-6-6" />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    );
}
