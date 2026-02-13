import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchProducts } from '../../api/services/product';
import { Plus, Edit2, Trash2, Search } from 'lucide-react';

export default function AdminProductList() {
    const [page, setPage] = useState(0);
    const [keyword, setKeyword] = useState('');

    const { data, isLoading } = useQuery({
        queryKey: ['admin-products', page, keyword],
        queryFn: () => fetchProducts(page, 20, undefined, undefined, keyword),
    });

    return (
        <div>
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-2xl font-bold">Products</h1>
                    <p className="text-stone-500 text-sm">Manage your product catalog</p>
                </div>
                <button className="flex items-center space-x-2 bg-black text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-stone-800 transition-all shadow-lg shadow-black/10">
                    <Plus size={18} />
                    <span>Add Product</span>
                </button>
            </div>

            <div className="bg-white rounded-[20px] shadow-sm border border-stone-100 overflow-hidden">
                {/* Toolbar */}
                <div className="p-5 border-b border-stone-100 flex items-center justify-between bg-stone-50/50">
                    <div className="relative">
                        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-stone-400" />
                        <input
                            type="text"
                            placeholder="Search products..."
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            className="pl-9 pr-4 py-2 bg-white border border-stone-200 rounded-lg text-sm focus:ring-1 focus:ring-black focus:border-black outline-none w-64 transition-all"
                        />
                    </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                    <table className="w-full text-sm text-left">
                        <thead className="bg-stone-50 text-stone-500 font-medium border-b border-stone-100">
                            <tr>
                                <th className="px-6 py-4 w-16">ID</th>
                                <th className="px-6 py-4">Product</th>
                                <th className="px-6 py-4">Category</th>
                                <th className="px-6 py-4">Price</th>
                                <th className="px-6 py-4">Status</th>
                                <th className="px-6 py-4 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-stone-50">
                            {isLoading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={6} className="px-6 py-4">
                                            <div className="h-10 bg-stone-100 rounded-lg" />
                                        </td>
                                    </tr>
                                ))
                            ) : data?.content.map((product) => (
                                <tr key={product.id} className="hover:bg-stone-50/50 transition-colors">
                                    <td className="px-6 py-4 text-stone-500">#{product.id}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center space-x-4">
                                            <div className="w-10 h-10 rounded-lg bg-stone-100 overflow-hidden flex-shrink-0">
                                                <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                                            </div>
                                            <div className="font-medium text-stone-900">{product.name}</div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className="px-2.5 py-1 rounded-md bg-stone-100 text-stone-600 text-xs font-bold border border-stone-200">
                                            {product.category}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 font-medium">₩{product.price.toLocaleString()}</td>
                                    <td className="px-6 py-4">
                                        {/* Mock status based on stock */}
                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${product.badge === 'Low Stock'
                                            ? 'bg-yellow-100 text-yellow-800'
                                            : 'bg-green-100 text-green-800'
                                            }`}>
                                            {product.badge === 'Low Stock' ? 'Low Stock' : 'Active'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <div className="flex items-center justify-end space-x-2">
                                            <button className="p-2 text-stone-400 hover:text-black hover:bg-stone-100 rounded-lg transition-colors">
                                                <Edit2 size={16} />
                                            </button>
                                            <button className="p-2 text-stone-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors">
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                {/* Pagination */}
                {data && data.totalPages > 1 && (
                    <div className="p-4 border-t border-stone-100 flex justify-center space-x-2">
                        <button
                            onClick={() => setPage(p => Math.max(0, p - 1))}
                            disabled={page === 0}
                            className="px-3 py-1.5 rounded-lg border border-stone-200 text-xs disabled:opacity-30 hover:bg-stone-50 transition-colors"
                        >
                            Previous
                        </button>
                        <span className="px-3 py-1.5 text-xs font-medium text-stone-500">
                            Page {page + 1} of {data.totalPages}
                        </span>
                        <button
                            onClick={() => setPage(p => Math.min(data.totalPages - 1, p + 1))}
                            disabled={page >= data.totalPages - 1}
                            className="px-3 py-1.5 rounded-lg border border-stone-200 text-xs disabled:opacity-30 hover:bg-stone-50 transition-colors"
                        >
                            Next
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
}
