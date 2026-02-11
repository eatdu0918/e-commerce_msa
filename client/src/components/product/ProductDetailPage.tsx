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

    const { data: product, isLoading } = useQuery({
        queryKey: ['product', id],
        queryFn: () => getProduct(Number(id)),
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

    if (isLoading || !product) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-black"></div>
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
                    <span className="text-sm font-medium">돌아가기</span>
                </button>

                <div className="grid md:grid-cols-2 gap-16 items-start">
                    <div className="aspect-[4/5] bg-stone-100 rounded-[40px] overflow-hidden shadow-sm">
                        <img src={product.image} alt={product.name} className="w-full h-full object-cover" />
                    </div>

                    <div className="text-left">
                        <p className="text-xs text-stone-400 uppercase tracking-widest mb-2 font-bold">{product.category}</p>
                        <h2 className="text-4xl font-bold mb-4 tracking-tight text-stone-900">{product.name}</h2>
                        <p className="text-2xl font-light mb-8 text-stone-800">₩{product.price.toLocaleString()}</p>
                        <p className="text-stone-500 mb-12 leading-relaxed max-w-md">{product.description}</p>

                        <div className="space-y-8 mb-12">
                            <div>
                                <p className="text-xs font-bold mb-4 uppercase tracking-wider text-stone-400">SIZE</p>
                                <div className="flex space-x-3">
                                    {sizes.map(size => (
                                        <button
                                            key={size}
                                            onClick={() => setSelectedSize(size)}
                                            className={`w-12 h-12 rounded-full text-xs font-bold border transition-all ${selectedSize === size
                                                ? 'bg-black text-white border-black'
                                                : 'border-stone-200 text-stone-600 hover:border-black'
                                                }`}
                                        >
                                            {size}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            <div>
                                <p className="text-xs font-bold mb-4 uppercase tracking-wider text-stone-400">QUANTITY</p>
                                <div className="flex items-center space-x-4 border border-stone-200 w-fit rounded-full px-4 py-2 bg-white">
                                    <button
                                        onClick={() => setQuantity((prev: number) => Math.max(1, prev - 1))}
                                        className="text-stone-400 hover:text-black transition-colors"
                                    >
                                        <Minus size={16} />
                                    </button>
                                    <span className="text-sm font-bold w-6 text-center">{quantity}</span>
                                    <button
                                        onClick={() => setQuantity((prev: number) => prev + 1)}
                                        className="text-stone-400 hover:text-black transition-colors"
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
                                disabled={addCartMutation.isPending}
                                className="flex-1 bg-stone-100 py-5 rounded-2xl font-bold hover:bg-stone-200 transition-colors text-stone-900 flex items-center justify-center space-x-2"
                            >
                                {addCartMutation.isPending && <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-black mr-2"></div>}
                                <span>{addCartMutation.isPending ? t('common.adding') || 'ADDING...' : t('common.add_to_cart')}</span>
                            </button>
                            <button
                                onClick={handleBuyClick}
                                className="flex-1 bg-black text-white py-5 rounded-2xl font-bold hover:bg-stone-800 transition-all shadow-lg shadow-black/10"
                            >
                                {t('common.buy_now') || 'BUY NOW'}
                            </button>
                        </div>
                    </div>
                </div>

                <div className="mt-20 border-t border-stone-200 pt-16">
                    <div className="flex justify-between items-center mb-8">
                        <h3 className="text-2xl font-bold text-left">Reviews</h3>
                        {user && !isReviewFormOpen && (
                            <button
                                onClick={() => setIsReviewFormOpen(true)}
                                className="px-6 py-2 bg-black text-white rounded-xl text-sm font-bold hover:bg-stone-800 transition-all"
                            >
                                Write a Review
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
                        navigate(`/checkout/${orderId}`); // Or wherever we need to go
                    }}
                />
            )}
        </div>
    );
}
