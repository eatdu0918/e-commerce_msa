import { Trash2, ShoppingBag, ChevronRight } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getWishlist, removeFromWishlist } from '../../api/services/wishlist';
import { addToCart } from '../../api/services/cart';
import type { WishlistItemResponse } from '../../api/services/wishlist';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

export default function WishlistView() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data: wishlistItems, isLoading } = useQuery<WishlistItemResponse[]>({
        queryKey: ['wishlist'],
        queryFn: getWishlist,
    });

    const removeMutation = useMutation({
        mutationFn: (productId: number) => removeFromWishlist(productId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['wishlist'] });
        },
    });

    const addCartMutation = useMutation({
        mutationFn: (productId: number) => addToCart({ productId, quantity: 1 }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['cart'] });
            toast.success(t('cart.wish_to_cart_success') || '장바구니에 담겼습니다.');
        },
    });

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-20">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
            </div>
        );
    }

    if (!wishlistItems || wishlistItems.length === 0) {
        return (
            <div className="text-center py-20 text-stone-500">
                <div className="w-16 h-16 bg-stone-50 rounded-full flex items-center justify-center mx-auto mb-6">
                    <Trash2 size={24} className="text-stone-300" />
                </div>
                <p>{t('order.no_history') || '찜한 상품이 없습니다.'}</p>
            </div>
        );
    }

    return (
        <div className="max-w-5xl mx-auto px-6 py-12 grid gap-6">
            {wishlistItems.map((item) => (
                <div
                    key={item.wishlistItemId}
                    className="group bg-white border border-stone-100 p-6 rounded-[30px] flex items-center space-x-6 hover:shadow-md transition-all"
                >
                    <div
                        className="w-24 h-24 bg-stone-100 rounded-2xl overflow-hidden cursor-pointer flex-shrink-0"
                        onClick={() => navigate(`/product/${item.productId}`)}
                    >
                        {item.imageUrl ? (
                            <img
                                src={item.imageUrl}
                                alt={item.productName}
                                className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                            />
                        ) : (
                            <div className="w-full h-full flex items-center justify-center text-stone-300">
                                <ShoppingBag size={24} />
                            </div>
                        )}
                    </div>

                    <div className="flex-1 text-left min-w-0">
                        <div className="flex justify-between items-start">
                            <div>
                                <h4
                                    className="font-bold text-lg cursor-pointer hover:text-stone-600 transition-colors line-clamp-1"
                                    onClick={() => navigate(`/product/${item.productId}`)}
                                >
                                    {item.productName}
                                </h4>
                                <p className="text-stone-400 text-xs mt-1 line-clamp-1">{item.productDescription}</p>
                            </div>
                            <button
                                onClick={() => removeMutation.mutate(item.productId)}
                                className="p-2 text-stone-200 hover:text-red-500 transition-colors"
                            >
                                <Trash2 size={18} />
                            </button>
                        </div>

                        <div className="flex items-center justify-between mt-4">
                            <span className="font-bold text-stone-900">${item.price.toLocaleString()}</span>
                            <div className="flex space-x-2">
                                <button
                                    onClick={() => addCartMutation.mutate(item.productId)}
                                    className="p-2 bg-stone-50 text-stone-500 hover:bg-stone-900 hover:text-white rounded-full transition-all"
                                    title={t('common.add_to_cart')}
                                >
                                    <ShoppingBag size={18} />
                                </button>
                                <button
                                    onClick={() => navigate(`/product/${item.productId}`)}
                                    className="p-2 bg-stone-50 text-stone-500 hover:bg-stone-900 hover:text-white rounded-full transition-all"
                                >
                                    <ChevronRight size={18} />
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
}
