import { X, Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCart, updateCartItem, removeFromCart } from '../../api/services/cart';
import type { CartResponse } from '../../api/services/cart';
import { useTranslation } from 'react-i18next';
import { useScrollLock } from '../../hooks/useScrollLock';

interface CartModalProps {
    isOpen: boolean;
    onClose: () => void;
    onCheckout: () => void;
}

export default function CartModal({ isOpen, onClose, onCheckout }: CartModalProps) {
    const { t, i18n } = useTranslation();
    const queryClient = useQueryClient();

    useScrollLock(isOpen);

    const { data: cart, isLoading } = useQuery<CartResponse>({
        queryKey: ['cart', i18n.language],
        queryFn: getCart,
        enabled: isOpen,
    });

    const updateQuantityMutation = useMutation({
        mutationFn: ({ productId, quantity }: { productId: number; quantity: number }) =>
            updateCartItem(productId, { quantity }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['cart'] });
        },
    });

    const removeMutation = useMutation({
        mutationFn: (productId: number) => removeFromCart(productId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['cart'] });
        },
    });

    if (!isOpen) return null;

    const items = cart?.items || [];
    const totalPrice = cart?.totalPrice || 0;

    return (
        <div className="fixed inset-0 z-[200] overflow-hidden">
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm transition-opacity" onClick={onClose} />

            <div className="absolute inset-y-0 right-0 max-w-full flex">
                <div className="w-screen max-w-md bg-white shadow-2xl flex flex-col transform transition-transform duration-500 ease-in-out">
                    <div className="flex-1 flex flex-col min-h-0">
                        {/* Header */}
                        <div className="px-6 py-6 border-b border-stone-100 flex items-center justify-between">
                            <div className="flex items-center space-x-3">
                                <ShoppingBag className="text-black" size={24} />
                                <h2 className="text-xl font-bold tracking-tight">{t('common.cart') || 'Cart'}</h2>
                                {items.length > 0 && (
                                    <span className="bg-stone-100 text-stone-500 text-xs px-2 py-0.5 rounded-full font-bold">
                                        {items.length}
                                    </span>
                                )}
                            </div>
                            <button
                                onClick={onClose}
                                className="p-2 hover:bg-stone-50 rounded-full transition-colors"
                            >
                                <X size={24} />
                            </button>
                        </div>

                        {/* Content */}
                        <div className="flex-1 overflow-y-auto px-6 py-8 no-scrollbar">
                            {isLoading ? (
                                <div className="flex justify-center items-center h-40">
                                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-black"></div>
                                </div>
                            ) : items.length === 0 ? (
                                <div className="text-center py-20">
                                    <div className="w-20 h-20 bg-stone-50 rounded-full flex items-center justify-center mx-auto mb-6">
                                        <ShoppingBag size={32} className="text-stone-300" />
                                    </div>
                                    <p className="text-stone-500 font-medium mb-8">{t('cart.empty_msg')}</p>
                                    <button
                                        onClick={onClose}
                                        className="bg-black text-white px-8 py-3 rounded-full text-sm font-bold hover:bg-stone-800 transition-colors"
                                    >
                                        {t('cart.continue_shopping')}
                                    </button>
                                </div>
                            ) : (
                                <div className="space-y-8">
                                    {items.map((item) => (
                                        <div key={item.cartItemId} className="flex space-x-6 group">
                                            <div className="w-24 h-24 bg-stone-100 rounded-2xl overflow-hidden flex-shrink-0 relative">
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

                                            <div className="flex-1 flex flex-col justify-between py-1">
                                                <div>
                                                    <div className="flex justify-between">
                                                        <h3 className="font-bold text-sm line-clamp-1">{item.productName}</h3>
                                                        <button
                                                            onClick={() => removeMutation.mutate(item.productId)}
                                                            className="text-stone-300 hover:text-red-500 transition-colors"
                                                        >
                                                            <Trash2 size={16} />
                                                        </button>
                                                    </div>
                                                    <p className="text-xs text-stone-400 mt-1 line-clamp-1">{item.productDescription}</p>
                                                </div>

                                                <div className="flex items-center justify-between mt-4">
                                                    <div className="flex items-center bg-stone-50 rounded-full px-2 py-1 border border-stone-100">
                                                        <button
                                                            onClick={() => item.quantity > 1 && updateQuantityMutation.mutate({ productId: item.productId, quantity: item.quantity - 1 })}
                                                            className="p-1 hover:text-black text-stone-400 transition-colors"
                                                            disabled={item.quantity <= 1}
                                                        >
                                                            <Minus size={14} />
                                                        </button>
                                                        <span className="w-8 text-center text-xs font-bold">{item.quantity}</span>
                                                        <button
                                                            onClick={() => updateQuantityMutation.mutate({ productId: item.productId, quantity: item.quantity + 1 })}
                                                            className="p-1 hover:text-black text-stone-400 transition-colors"
                                                            disabled={item.quantity >= item.stockQuantity}
                                                        >
                                                            <Plus size={14} />
                                                        </button>
                                                    </div>
                                                    <p className="font-bold text-sm">{(item.price * item.quantity).toLocaleString()}{t('cart.currency')}</p>
                                                </div>
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Footer */}
                        {items.length > 0 && (
                            <div className="px-6 py-8 bg-white border-t border-stone-100">
                                <div className="flex justify-between items-center mb-6">
                                    <span className="text-stone-400 font-medium">{t('cart.total_amount')}</span>
                                    <span className="text-2xl font-bold tracking-tight">{totalPrice.toLocaleString()}{t('cart.currency')}</span>
                                </div>
                                <button
                                    onClick={onCheckout}
                                    className="w-full bg-black text-white py-4 rounded-[20px] text-sm font-bold hover:bg-stone-800 transition-all active:scale-[0.98]"
                                >
                                    {t('cart.checkout')}
                                </button>
                                <p className="text-center text-[10px] text-stone-400 mt-4 italic">
                                    {t('cart.shipping_info_notice')}
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
