import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '../../api/services/admin';
import type { Product, CreateProductRequest, UpdateProductRequest } from '../../api/services/admin';
import { getAllCategories } from '../../api/services/category';
import type { CategoryResponse } from '../../api/services/category';
import { Plus, Edit2, Trash2, Search, X } from 'lucide-react';
import { useScrollLock } from '../../hooks/useScrollLock';

interface ProductFormData {
    name: string;
    description: string;
    price: string;
    stockQuantity: string;
    categoryId: string;
    imageUrl: string;
}

const initialFormData: ProductFormData = {
    name: '',
    description: '',
    price: '',
    stockQuantity: '',
    categoryId: '',
    imageUrl: '',
};

export default function AdminProductList() {
    const [page, setPage] = useState(0);
    const [keyword, setKeyword] = useState('');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingProduct, setEditingProduct] = useState<Product | null>(null);
    const [formData, setFormData] = useState<ProductFormData>(initialFormData);
    const queryClient = useQueryClient();

    useScrollLock(isModalOpen);

    const { data, isLoading } = useQuery({
        queryKey: ['admin-products', page, keyword],
        queryFn: () => adminApi.getProducts(page, 20, keyword || undefined),
    });

    const { data: categories } = useQuery({
        queryKey: ['categories'],
        queryFn: getAllCategories,
    });

    const createMutation = useMutation({
        mutationFn: (data: CreateProductRequest) => adminApi.createProduct(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-products'] });
            closeModal();
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: UpdateProductRequest }) =>
            adminApi.updateProduct(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-products'] });
            closeModal();
        },
    });

    const deleteMutation = useMutation({
        mutationFn: adminApi.deleteProduct,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-products'] });
        },
    });

    const openCreateModal = () => {
        setEditingProduct(null);
        setFormData(initialFormData);
        setIsModalOpen(true);
    };

    const openEditModal = (product: Product) => {
        setEditingProduct(product);
        setFormData({
            name: product.name,
            description: product.description,
            price: product.price.toString(),
            stockQuantity: product.stockQuantity.toString(),
            categoryId: product.categoryId.toString(),
            imageUrl: product.imageUrl || '',
        });
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setEditingProduct(null);
        setFormData(initialFormData);
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const payload = {
            name: formData.name,
            description: formData.description,
            price: parseFloat(formData.price),
            stockQuantity: parseInt(formData.stockQuantity),
            categoryId: parseInt(formData.categoryId),
            imageUrl: formData.imageUrl || undefined,
        };

        if (editingProduct) {
            updateMutation.mutate({ id: editingProduct.id, data: payload });
        } else {
            createMutation.mutate(payload);
        }
    };

    const handleDelete = (id: number) => {
        if (window.confirm('Are you sure you want to delete this product?')) {
            deleteMutation.mutate(id);
        }
    };

    const flattenCategories = (cats: CategoryResponse[]): CategoryResponse[] => {
        const result: CategoryResponse[] = [];
        const flatten = (items: CategoryResponse[], prefix = '') => {
            items.forEach((cat) => {
                result.push({ ...cat, name: prefix + cat.name });
                if (cat.children?.length) {
                    flatten(cat.children, prefix + '  ');
                }
            });
        };
        flatten(cats);
        return result;
    };

    const flatCategories = categories ? flattenCategories(categories) : [];

    return (
        <div>
            <div className="flex justify-between items-center mb-8">
                <div>
                    <h1 className="text-2xl font-bold">Products</h1>
                    <p className="text-stone-500 text-sm">Manage your product catalog</p>
                </div>
                <button
                    onClick={openCreateModal}
                    className="flex items-center space-x-2 bg-black text-white px-5 py-2.5 rounded-xl text-sm font-bold hover:bg-stone-800 transition-all shadow-lg shadow-black/10"
                >
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
                    <span className="text-sm text-stone-400">
                        Total {data?.data?.totalElements || 0} products
                    </span>
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
                                <th className="px-6 py-4">Stock</th>
                                <th className="px-6 py-4">Status</th>
                                <th className="px-6 py-4 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-stone-50">
                            {isLoading ? (
                                [...Array(5)].map((_, i) => (
                                    <tr key={i} className="animate-pulse">
                                        <td colSpan={7} className="px-6 py-4">
                                            <div className="h-10 bg-stone-100 rounded-lg" />
                                        </td>
                                    </tr>
                                ))
                            ) : data?.data?.content.map((product) => (
                                <tr key={product.id} className="hover:bg-stone-50/50 transition-colors">
                                    <td className="px-6 py-4 text-stone-500">#{product.id}</td>
                                    <td className="px-6 py-4">
                                        <div className="flex items-center space-x-4">
                                            <div className="w-10 h-10 rounded-lg bg-stone-100 overflow-hidden flex-shrink-0">
                                                {product.imageUrl ? (
                                                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                                                ) : (
                                                    <div className="w-full h-full flex items-center justify-center text-stone-300 text-xs">No img</div>
                                                )}
                                            </div>
                                            <div>
                                                <div className="font-medium text-stone-900">{product.name}</div>
                                                <div className="text-xs text-stone-400 truncate max-w-[200px]">{product.description}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className="px-2.5 py-1 rounded-md bg-stone-100 text-stone-600 text-xs font-bold border border-stone-200">
                                            {product.categoryName}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 font-medium">₩{product.price.toLocaleString()}</td>
                                    <td className="px-6 py-4">
                                        <span className={`font-medium ${product.stockQuantity < 10 ? 'text-orange-500' : 'text-stone-700'}`}>
                                            {product.stockQuantity}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4">
                                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                                            product.isActive
                                                ? 'bg-green-100 text-green-800'
                                                : 'bg-red-100 text-red-800'
                                        }`}>
                                            {product.isActive ? 'Active' : 'Inactive'}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <div className="flex items-center justify-end space-x-2">
                                            <button
                                                onClick={() => openEditModal(product)}
                                                className="p-2 text-stone-400 hover:text-black hover:bg-stone-100 rounded-lg transition-colors"
                                            >
                                                <Edit2 size={16} />
                                            </button>
                                            <button
                                                onClick={() => handleDelete(product.id)}
                                                disabled={deleteMutation.isPending}
                                                className="p-2 text-stone-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                                            >
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
                {data?.data && data.data.totalPages > 1 && (
                    <div className="p-4 border-t border-stone-100 flex justify-center space-x-2">
                        <button
                            onClick={() => setPage(p => Math.max(0, p - 1))}
                            disabled={page === 0}
                            className="px-3 py-1.5 rounded-lg border border-stone-200 text-xs disabled:opacity-30 hover:bg-stone-50 transition-colors"
                        >
                            Previous
                        </button>
                        <span className="px-3 py-1.5 text-xs font-medium text-stone-500">
                            Page {page + 1} of {data.data.totalPages}
                        </span>
                        <button
                            onClick={() => setPage(p => Math.min(data.data.totalPages - 1, p + 1))}
                            disabled={page >= data.data.totalPages - 1}
                            className="px-3 py-1.5 rounded-lg border border-stone-200 text-xs disabled:opacity-30 hover:bg-stone-50 transition-colors"
                        >
                            Next
                        </button>
                    </div>
                )}
            </div>

            {/* Modal */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white rounded-2xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
                        <div className="p-6 border-b border-stone-100 flex justify-between items-center">
                            <h2 className="text-xl font-bold">
                                {editingProduct ? 'Edit Product' : 'Add New Product'}
                            </h2>
                            <button
                                onClick={closeModal}
                                className="p-2 hover:bg-stone-100 rounded-lg transition-colors"
                            >
                                <X size={20} />
                            </button>
                        </div>
                        <form onSubmit={handleSubmit} className="p-6 space-y-5">
                            <div>
                                <label className="block text-xs font-bold text-stone-500 uppercase mb-2">
                                    Name *
                                </label>
                                <input
                                    type="text"
                                    value={formData.name}
                                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                                    required
                                    className="w-full border border-stone-200 rounded-xl px-4 py-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none"
                                    placeholder="Product name"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-stone-500 uppercase mb-2">
                                    Description *
                                </label>
                                <textarea
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    required
                                    rows={3}
                                    className="w-full border border-stone-200 rounded-xl px-4 py-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none resize-none"
                                    placeholder="Product description"
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-xs font-bold text-stone-500 uppercase mb-2">
                                        Price *
                                    </label>
                                    <input
                                        type="number"
                                        value={formData.price}
                                        onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                                        required
                                        min="0"
                                        step="0.01"
                                        className="w-full border border-stone-200 rounded-xl px-4 py-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none"
                                        placeholder="0"
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-bold text-stone-500 uppercase mb-2">
                                        Stock Quantity *
                                    </label>
                                    <input
                                        type="number"
                                        value={formData.stockQuantity}
                                        onChange={(e) => setFormData({ ...formData, stockQuantity: e.target.value })}
                                        required
                                        min="0"
                                        className="w-full border border-stone-200 rounded-xl px-4 py-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none"
                                        placeholder="0"
                                    />
                                </div>
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-stone-500 uppercase mb-2">
                                    Category *
                                </label>
                                <select
                                    value={formData.categoryId}
                                    onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                                    required
                                    className="w-full border border-stone-200 rounded-xl px-4 py-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none bg-white"
                                >
                                    <option value="">Select a category</option>
                                    {flatCategories.map((cat) => (
                                        <option key={cat.id} value={cat.id}>
                                            {cat.name}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div>
                                <label className="block text-xs font-bold text-stone-500 uppercase mb-2">
                                    Image URL
                                </label>
                                <input
                                    type="url"
                                    value={formData.imageUrl}
                                    onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                                    className="w-full border border-stone-200 rounded-xl px-4 py-3 text-sm focus:ring-1 focus:ring-black focus:border-black outline-none"
                                    placeholder="https://example.com/image.jpg"
                                />
                            </div>
                            <div className="flex justify-end space-x-3 pt-4">
                                <button
                                    type="button"
                                    onClick={closeModal}
                                    className="px-5 py-2.5 text-sm font-medium text-stone-600 hover:bg-stone-100 rounded-xl transition-colors"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={createMutation.isPending || updateMutation.isPending}
                                    className="px-5 py-2.5 text-sm font-bold bg-black text-white rounded-xl hover:bg-stone-800 transition-colors disabled:opacity-50"
                                >
                                    {createMutation.isPending || updateMutation.isPending
                                        ? 'Saving...'
                                        : editingProduct
                                        ? 'Update Product'
                                        : 'Create Product'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
