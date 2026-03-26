import { useState } from 'react';
import { ChevronLeft, Minus, Plus, Heart } from 'lucide-react';
import OrderModal from '../order/OrderModal';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMyProfile } from '../../api/services/user';
import { addToCart } from '../../api/services/cart';
import { addToWishlist, removeFromWishlist, isInWishlist } from '../../api/services/wishlist';
import { useTranslation } from 'react-i18next';
import { useParams, useNavigate, useOutletContext } from 'react-router-dom';
import { getProduct } from '../../api/services/product';
import { getStock } from '../../api/services/stock';
import ReviewList from '../review/ReviewList';
import ReviewForm from '../review/ReviewForm';
import { Helmet } from 'react-helmet-async';

interface OutletContextType {
    openLogin: () => void;
    openCart: () => void;
}

export default function ProductDetailPage() {
    const { t } = useTranslation();
    const { id } = useParams();
    const navigate = useNavigate();
    const { openLogin, openCart } = useOutletContext<OutletContextType>();

    const [selectedSize, setSelectedSize] = useState('S');
    const [quantity, setQuantity] = useState(1);
    const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
    const [isReviewFormOpen, setIsReviewFormOpen] = useState(false);
    const queryClient = useQueryClient();

    const { data: product, isLoading, isError } = useQuery({
        queryKey: ['product', id],
        queryFn: () => getProduct(Number(id)),
        enabled: !!id,
    });

    const { data: stockInfo } = useQuery({
        queryKey: ['stock', id],
        queryFn: () => getStock(Number(id)),
        enabled: !!id,
    });

    const { data: user } = useQuery({ queryKey: ['user'], queryFn: getMyProfile, retry: false });

    const { data: isFavorite } = useQuery({
        queryKey: ['wishlist-check', id],
        queryFn: () => isInWishlist(Number(id)),
        enabled: !!user && !!id,
    });

    const addCartMutation = useMutation({
        mutationFn: () => addToCart({ productId: Number(id), quantity }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['cart'] });
            openCart();
        },
        onError: (err: any) => {
            // If 401, prompt login
            if (err.response?.status === 401) {
                openLogin();
            } else {
                alert('Failed to add to cart');
            }
        }
    });

    const wishlistMutation = useMutation({
        mutationFn: async () => {
            if (isFavorite) {
                await removeFromWishlist(Number(id));
            } else {
                await addToWishlist({ productId: Number(id) });
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['wishlist-check', id] });
            queryClient.invalidateQueries({ queryKey: ['wishlist'] });
        },
    });

    const sizes = ['S', 'M', 'L'];

    const handleAddToCart = () => {
        if (!user) {
            openLogin();
            return;
        }
        addCartMutation.mutate();
    };

    const handleWishlistToggle = () => {
        if (!user) {
            openLogin();
            return;
        }
        wishlistMutation.mutate();
    };

    const handleBuyClick = () => {
        if (!user) {
            openLogin();
            return;
        }
        setIsOrderModalOpen(true);
    };

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-black"></div>
            </div>
        );
    }

    if (isError || !product) {
        return (
            <div className="min-h-screen pt-32 pb-12 flex flex-col items-center justify-center text-center px-4 bg-[#f9f7f2]">
                <h2 className="text-3xl font-bold mb-4">{t('product.not_found')}</h2>
                <p className="text-stone-500 mb-8">{t('product.not_found_desc')}</p>
                <button
                    onClick={() => navigate('/shop')}
                    className="bg-black text-white px-8 py-3 rounded-full font-bold hover:bg-stone-800 transition-all"
                >
                    {t('common.shop_all')}
                </button>
            </div>
        );
    }

    return (
        <div className="min-h-screen py-16 bg-[#f9f7f2]">
            <Helmet>
                <title>{`${product.name} | Sparta Shop`}</title>
                <meta name="description" content={product.description} />
            </Helmet>
            <div className="max-w-7xl mx-auto px-6">
                <button
                    onClick={() => navigate(-1)}
                    className="flex items-center text-stone-400 hover:text-black transition-colors mb-12 group"
                >
                    <ChevronLeft size={20} className="mr-2 transition-transform group-hover:-translate-x-1" />
                    <span className="text-sm font-medium">{t('product.back')}</span>
                </button>

                <div className="grid md:grid-cols-2 gap-16 items-start">
                    <div className="aspect-[4/5] bg-stone-100 rounded-[40px] overflow-hidden shadow-sm">
                        <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                    </div>

                    <div className="text-left">
                        <p className="text-xs text-stone-400 uppercase tracking-widest mb-2 font-bold">{product.category}</p>
                        <h2 className="text-4xl font-bold mb-4 tracking-tight text-stone-900">{product.name}</h2>
                        <p className="text-2xl font-light mb-4 text-stone-800">${product.price.toLocaleString()}</p>
                        {stockInfo && (
                            <div className="mb-6">
                                {stockInfo.quantity > 10 ? (
                                    <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-green-100 text-green-700">
                                        {t('product.in_stock', { count: stockInfo.quantity })}
                                    </span>
                                ) : stockInfo.quantity > 0 ? (
                                    <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-yellow-100 text-yellow-700">
                                        {t('product.low_stock', { count: stockInfo.quantity })}
                                    </span>
                                ) : (
                                    <span className="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-red-100 text-red-700">
                                        {t('product.out_of_stock')}
                                    </span>
                                )}
                            </div>
                        )}
                        <p className="text-stone-500 mb-12 leading-relaxed max-w-md">{product.description}</p>

                        <div className="space-y-8 mb-12">
                            <div>
                                <p className="text-xs font-bold mb-4 uppercase tracking-wider text-stone-400">{t('product.size')}</p>
                                <div className="flex space-x-3">
                                    {sizes.map(size => (
                                        <button
                                            key={size}
                                            onClick={() => setSelectedSize(size)}
                                            className={`w-12 h-12 rounded-full text-xs font-bold border transition-all ${selectedSize === size
                                                ? 'bg-black text-white border-black'
                                                : 'border-stone-200 text-stone600 hover:border-black'
                                                }`}
                                        >
                                            {size}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div>
                                <p className="text-xs font-bold mb-4 uppercase tracking-wider text-stone-400">{t('product.quantity')}</p>
                                <div className={`flex items-center space-x-4 border border-stone-200 w-fit rounded-full px-4 py-2 bg-white ${(!stockInfo || stockInfo.quantity === 0) ? 'opacity-50 cursor-not-allowed' : ''}`}>
                                    <button
                                        onClick={() => setQuantity((prev: number) => Math.max(1, prev - 1))}
                                        disabled={!stockInfo || stockInfo.quantity === 0 || quantity <= 1}
                                        className="text-stone-400 hover:text-black transition-colors disabled:cursor-not-allowed"
                                    >
                                        <Minus size={16} />
                                    </button>
                                    <span className="text-sm font-bold w-6 text-center">{quantity}</span>
                                    <button
                                        onClick={() => setQuantity((prev: number) => prev + 1)}
                                        disabled={!stockInfo || stockInfo.quantity === 0 || quantity >= (stockInfo?.quantity || 0)}
                                        className="text-stone-400 hover:text-black transition-colors disabled:cursor-not-allowed"
                                    >
                                        <Plus size={16} />
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="flex space-x-4">
                            <button
                                onClick={handleWishlistToggle}
                                className={`w-16 flex items-center justify-center rounded-2xl border transition-all ${isFavorite ? 'bg-red-50 border-red-100 text-red-500' : 'bg-stone-50 border-stone-100 text-stone-300 hover:text-stone-500'}`}
                            >
                                <Heart size={24} fill={isFavorite ? 'currentColor' : 'none'} />
                            </button>
                            <button
                                onClick={handleAddToCart}
                                disabled={addCartMutation.isPending || !stockInfo || stockInfo.quantity === 0 || quantity > stockInfo.quantity}
                                className="flex-1 bg-stone-100 py-5 rounded-2xl font-bold hover:bg-stone-200 transition-colors text-stone-900 flex items-center justify-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {addCartMutation.isPending && <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-black mr-2"></div>}
                                <span>{addCartMutation.isPending ? t('common.adding') || 'ADDING...' : t('common.add_to_cart')}</span>
                            </button>
                            <button
                                onClick={handleBuyClick}
                                disabled={!stockInfo || stockInfo.quantity === 0 || quantity > stockInfo.quantity}
                                className="flex-1 bg-black text-white py-5 rounded-2xl font-bold hover:bg-stone-800 transition-all shadow-lg shadow-black/10 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {t('common.buy_now') || 'BUY NOW'}
                            </button>
                        </div>
                    </div>
                </div>

                <div className="mt-20 border-t border-stone-200 pt-16">
                    <div className="flex justify-between items-center mb-8">
                        <h3 className="text-2xl font-bold text-left">{t('product.reviews')}</h3>
                        {user && !isReviewFormOpen && (
                            <button
                                onClick={() => setIsReviewFormOpen(true)}
                                className="px-6 py-2 bg-black text-white rounded-xl text-sm font-bold hover:bg-stone-800 transition-all"
                            >
                                {t('product.write_review')}
                            </button>
                        )}
                    </div>

                    {isReviewFormOpen && (
                        <ReviewForm
                            productId={Number(id)}
                            onCancel={() => setIsReviewFormOpen(false)}
                        />
                    )}

                    <ReviewList productId={Number(id)} />
                </div>
            </div>

            {user && (
                <OrderModal
                    isOpen={isOrderModalOpen}
                    onClose={() => setIsOrderModalOpen(false)}
                    product={product}
                    quantity={quantity}
                    user={user}
                    onOrderSuccess={(orderId) => {
                        setIsOrderModalOpen(false);
                        navigate(`/order/complete/${orderId}`); // Or wherever we need to go
                    }}
                />
            )}
        </div>
    );
}
